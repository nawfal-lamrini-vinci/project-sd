import java.util.Objects;

public class Localisation {
    private long id;
    private double latitude;
    private double longitude;
    private String nom;
    private double altitude;

    public Localisation(long id, double latitude, double longitude, String nom, double altitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nom = nom;
        this.altitude = altitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Localisation)) return false;
        Localisation that = (Localisation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PointGeographique{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", nom='" + nom + '\'' +
                ", altitude=" + altitude +
                '}';
    }
}