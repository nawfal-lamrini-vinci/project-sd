import java.util.Objects;

public class Rue {
    private Localisation pointDepart;
    private Localisation pointArrivee;
    private double distance;
    private String nomRue;

    public Rue(String nomRue, double distance, Localisation pointDepart, Localisation pointArrivee) {
        this.nomRue = nomRue;
        this.distance = distance;
        this.pointDepart = pointDepart;
        this.pointArrivee = pointArrivee;
    }

    public Localisation getPointDepart() {
        return pointDepart;
    }

    public void setPointDepart(Localisation pointDepart) {
        this.pointDepart = pointDepart;
    }

    public Localisation getPointArrivee() {
        return pointArrivee;
    }

    public void setPointArrivee(Localisation pointArrivee) {
        this.pointArrivee = pointArrivee;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getNomRue() {
        return nomRue;
    }

    public void setNomRue(String nomRue) {
        this.nomRue = nomRue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rue)) return false;
        Rue rue = (Rue) o;
        return Objects.equals(pointDepart, rue.pointDepart)
                && Objects.equals(pointArrivee, rue.pointArrivee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointDepart, pointArrivee);
    }

    @Override
    public String toString() {
        return "Rue{" +
                "pointDepart=" + pointDepart +
                ", pointArrivee=" + pointArrivee +
                ", distance=" + distance +
                ", nomRue='" + nomRue + '\'' +
                '}';
    }
}