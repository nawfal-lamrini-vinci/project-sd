package domains.models;

public class Road {
  private final long   source;
  private final long   target;
  private final double distance;
  private final String streetName;

  public Road(long source, long target, double distance, String streetName) {
    this.source     = source;
    this.target     = target;
    this.distance   = distance;
    this.streetName = streetName;
  }

  public long   getSource()     { return source; }
  public long   getTarget()     { return target; }
  public double getDistance()   { return distance; }
  public String getStreetName() { return streetName; }
}