package domains.models;

public class Localisation {
  private final long   id;
  private final String name;
  private final double lat;
  private final double lon;
  private final double altitude;

  public Localisation(long id, String name, double lat, double lon, double altitude) {
    this.id       = id;
    this.name     = name;
    this.lat      = lat;
    this.lon      = lon;
    this.altitude = altitude;
  }

  public long   getId()       { return id; }
  public String getName()     { return name; }
  public double getLat()      { return lat; }
  public double getLon()      { return lon; }
  public double getAltitude() { return altitude; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Localisation)) return false;
    return id == ((Localisation) o).id;
  }

  @Override
  public int hashCode() { return Long.hashCode(id); }

  @Override
  public String toString() { return id + " (alt=" + altitude + ")"; }
}