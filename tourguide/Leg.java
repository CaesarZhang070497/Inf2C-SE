package tourguide;

/**
 * Created by asus on 2017/11/23.
 */
public class Leg{
    private String legIdentifier;
    private Annotation legAnnotation = Annotation.DEFAULT;

    public Leg(String legIdentifier, Annotation annotation) {
        this.legIdentifier = legIdentifier;
        this.legAnnotation = annotation;
    }

    public String getLegIdentifier() {
        return legIdentifier;
    }

    public Annotation getLegAnnotation() {
        return legAnnotation;
    }
}
