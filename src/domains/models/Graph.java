package domains.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;


/**
 * Graph representing a road network for flood simulation.
 *
 * Internal representation:
 *   - nodes  : HashMap<Long, Localisation>   → O(1) lookup by id
 *   - adjList: HashMap<Long, List<Road>>      → adjacency list (space-optimal for sparse graphs)
 *
 * All four algorithms use classical optimal data structures:
 *   Algo 1 – BFS              O(V + E)
 *   Algo 2 – BFS              O(V + E)
 *   Algo 3 – Dijkstra + PQ    O((V + E) log V)
 *   Algo 4 – Dijkstra + PQ    O((V + E) log V)
 */
public class Graph {

  // -----------------------------------------------------------------------
  // Internal storage
  // -----------------------------------------------------------------------
  private final HashMap<Long, Localisation> nodes;
  private final HashMap<Long, List<Road>>   adjList;

  // -----------------------------------------------------------------------
  // Constructor – loads CSV files
  // -----------------------------------------------------------------------
  public Graph(String localisations, String roads) {
    nodes   = new HashMap<>();
    adjList = new HashMap<>();
    loadNodes(localisations);
    loadEdges(roads);
  }

  // -----------------------------------------------------------------------
  // CSV loaders
  // -----------------------------------------------------------------------
  private void loadNodes(String filename) {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String line = br.readLine(); // skip header
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;
        String[] parts = line.split(",", 5);
        long   id  = Long.parseLong(parts[0].trim());
        String nm  = parts[1].trim();
        double lat = Double.parseDouble(parts[2].trim());
        double lon = Double.parseDouble(parts[3].trim());
        double alt = Double.parseDouble(parts[4].trim());
        Localisation loc = new Localisation(id, nm, lat, lon, alt);
        nodes.put(id, loc);
        adjList.put(id, new ArrayList<>());
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot load nodes file: " + filename, e);
    }
  }

  private void loadEdges(String filename) {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String line = br.readLine(); // skip header
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;
        String[] parts = line.split(",", 4);
        long   src  = Long.parseLong(parts[0].trim());
        long   tgt  = Long.parseLong(parts[1].trim());
        double dist = Double.parseDouble(parts[2].trim());
        String nm   = parts[3].trim();
        Road road = new Road(src, tgt, dist, nm);
        List<Road> neighbors = adjList.get(src);
        if (neighbors != null) neighbors.add(road);
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot load edges file: " + filename, e);
    }
  }

  // -----------------------------------------------------------------------
  // Helper
  // -----------------------------------------------------------------------
  private Localisation getNode(long id) {
    return nodes.get(id);
  }

  // -----------------------------------------------------------------------
  // Algorithm 1 – Flood Simulation (BFS)
  // Returns flooded nodes in BFS traversal order.
  // Propagation condition: alt(neighbor) <= alt(current) + epsilon
  // -----------------------------------------------------------------------
  public Localisation[] determinerZoneInondee(long[] idsDepart, double epsilon) {
    List<Localisation> result  = new ArrayList<>();
    Set<Long>          visited = new HashSet<>();

    Queue<Long> queue = new ArrayDeque<>();

    // Enqueue all starting points
    for (long id : idsDepart) {
      if (!visited.contains(id) && nodes.containsKey(id)) {
        visited.add(id);
        queue.add(id);
        result.add(nodes.get(id));
      }
    }

    while (!queue.isEmpty()) {
      long currentId = queue.poll();
      Localisation current = nodes.get(currentId);
      double maxAlt = current.getAltitude() + epsilon;

      for (Road road : adjList.get(currentId)) {
        long neighborId = road.getTarget();
        if (visited.contains(neighborId)) continue;
        Localisation neighbor = nodes.get(neighborId);
        if (neighbor == null) continue;

        // Propagation condition
        if (neighbor.getAltitude() <= maxAlt) {
          visited.add(neighborId);
          queue.add(neighborId);
          result.add(neighbor);
        }
      }
    }

    return result.toArray(new Localisation[0]);
  }

  // -----------------------------------------------------------------------
  // Algorithm 2 – Emergency Navigation (BFS, min number of roads)
  // Avoids all flooded nodes; returns path as a Deque (front = start).
  // -----------------------------------------------------------------------
  public Deque<Localisation> trouverCheminLePlusCourtPourContournerLaZoneInondee(
      long idDepart, long idArrivee, Localisation[] zoneInondee) {

    // Build a fast lookup set of flooded ids
    Set<Long> flooded = new HashSet<>();
    for (Localisation loc : zoneInondee) flooded.add(loc.getId());

    // Starting or ending node itself is flooded → no path
    if (flooded.contains(idDepart) || flooded.contains(idArrivee)) {
      return new ArrayDeque<>();
    }

    // BFS
    Map<Long, Long> parent = new HashMap<>();
    Queue<Long>     queue  = new ArrayDeque<>();

    parent.put(idDepart, null);
    queue.add(idDepart);

    while (!queue.isEmpty()) {
      long currentId = queue.poll();
      if (currentId == idArrivee) break;

      for (Road road : adjList.getOrDefault(currentId, Collections.emptyList())) {
        long neighborId = road.getTarget();
        if (parent.containsKey(neighborId)) continue;
        if (flooded.contains(neighborId)) continue;

        parent.put(neighborId, currentId);
        queue.add(neighborId);
      }
    }

    // Reconstruct path
    return reconstructPath(parent, idDepart, idArrivee);
  }

  // -----------------------------------------------------------------------
  // Algorithm 3 – Flood Chronology (Dijkstra)
  // Returns a LinkedHashMap ordered by ascending flood time.
  // Speed update: Vwater(p2) = Vwater(p1) + k * slope
  // Arc weight  : distance / Vwater  (travel time)
  // Stop propagation if Vwater <= 0.
  // -----------------------------------------------------------------------
  public Map<Localisation, Double> determinerChronologieDeLaCrue(
      long[] idsDepart, double vWaterInit, double k) {

    // dist[id] = earliest time water reaches id
    Map<Long, Double> dist = new HashMap<>();
    // vAtNode[id] = water speed when arriving at id
    Map<Long, Double> vAtNode = new HashMap<>();

    // PQ: [time, nodeId, currentSpeed]
    PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));

    for (long id : idsDepart) {
      if (!nodes.containsKey(id)) continue;
      dist.put(id, 0.0);
      vAtNode.put(id, vWaterInit);
      pq.offer(new double[]{0.0, id, vWaterInit});
    }

    while (!pq.isEmpty()) {
      double[] top      = pq.poll();
      double   time     = top[0];
      long     curId    = (long) top[1];
      double   curSpeed = top[2];

      // Already processed with a shorter time
      if (time > dist.getOrDefault(curId, Double.MAX_VALUE)) continue;

      Localisation cur = nodes.get(curId);

      for (Road road : adjList.getOrDefault(curId, Collections.emptyList())) {
        long   nbrId = road.getTarget();
        Localisation nbr = nodes.get(nbrId);
        if (nbr == null) continue;

        // Slope (positive = downhill)
        double slope = (cur.getAltitude() - nbr.getAltitude()) / road.getDistance();
        double newSpeed = curSpeed + k * slope;

        // Water stops if speed <= 0
        if (newSpeed <= 0) continue;

        double travelTime = road.getDistance() / newSpeed;
        double newTime    = time + travelTime;

        if (newTime < dist.getOrDefault(nbrId, Double.MAX_VALUE)) {
          dist.put(nbrId, newTime);
          vAtNode.put(nbrId, newSpeed);
          pq.offer(new double[]{newTime, nbrId, newSpeed});
        }
      }
    }

    // Build result map ordered by ascending time (LinkedHashMap preserves insertion order)
    List<Map.Entry<Long, Double>> entries = new ArrayList<>(dist.entrySet());
    entries.sort(Map.Entry.comparingByValue());

    Map<Localisation, Double> result = new LinkedHashMap<>();
    for (Map.Entry<Long, Double> e : entries) {
      result.put(nodes.get(e.getKey()), e.getValue());
    }
    return result;
  }

  // -----------------------------------------------------------------------
  // Algorithm 4 – Dynamic Evacuation (Dijkstra with time-dependent constraints)
  // Vehicle speed is constant (Vvehicule).
  // A node is forbidden if the vehicle would arrive after or at tFlood.
  // -----------------------------------------------------------------------
  public Deque<Localisation> trouverCheminDEvacuationLePlusCourt(
      long idDepart, long idEvacuation,
      double vVehicule, Map<Localisation, Double> tFlood) {

    // Build a fast id → floodTime lookup
    Map<Long, Double> floodTime = new HashMap<>();
    for (Map.Entry<Localisation, Double> e : tFlood.entrySet()) {
      floodTime.put(e.getKey().getId(), e.getValue());
    }

    // dist[id] = earliest time vehicle reaches id
    Map<Long, Double>  dist   = new HashMap<>();
    Map<Long, Long>    parent = new HashMap<>();

    PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));

    dist.put(idDepart, 0.0);
    parent.put(idDepart, null);
    pq.offer(new double[]{0.0, idDepart});

    while (!pq.isEmpty()) {
      double[] top   = pq.poll();
      double   time  = top[0];
      long     curId = (long) top[1];

      if (curId == idEvacuation) break;
      if (time > dist.getOrDefault(curId, Double.MAX_VALUE)) continue;

      for (Road road : adjList.getOrDefault(curId, Collections.emptyList())) {
        long   nbrId    = road.getTarget();
        double tArrival = time + road.getDistance() / vVehicule;

        // Cannot enter a flooded node
        Double tF = floodTime.get(nbrId);
        if (tF != null && tArrival >= tF) continue;

        if (tArrival < dist.getOrDefault(nbrId, Double.MAX_VALUE)) {
          dist.put(nbrId, tArrival);
          parent.put(nbrId, curId);
          pq.offer(new double[]{tArrival, nbrId});
        }
      }
    }

    return reconstructPath(parent, idDepart, idEvacuation);
  }

  // -----------------------------------------------------------------------
  // Shared path reconstruction helper
  // -----------------------------------------------------------------------
  private Deque<Localisation> reconstructPath(Map<Long, Long> parent, long start, long end) {
    Deque<Localisation> path = new ArrayDeque<>();
    if (!parent.containsKey(end)) return path; // no path found

    long cur = end;
    while (cur != start) {
      path.addFirst(nodes.get(cur));
      Long prev = parent.get(cur);
      if (prev == null) return new ArrayDeque<>(); // broken chain
      cur = prev;
    }
    path.addFirst(nodes.get(start));
    return path;
  }
}