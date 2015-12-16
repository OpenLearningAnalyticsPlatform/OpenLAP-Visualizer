package de.rwthaachen.openlap.visualizer.model;

import javax.persistence.*;

/**
 * The model representing the Data Transformer (Data Adapters) concrete implementations
 *
 * @author Bassim Bashir
 */
@Entity
@Table(name = "DATA_TRANSFORMER_METHODS")
public class DataTransformerMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "TRANSFORMER_METHOD_ID")
    private long id;
    @Column(nullable = false, name = "TRANSFORMER_METHOD_IMPLEMENTING_CLASS")
    private String implementingClassName;
    @Column(nullable = false, name = "TRANSFORMER_METHOD_NAME")
    private String name;
    @OneToOne(mappedBy = "dataTransformerMethod")
    private VisualizationMethod visualizationMethod;

    private DataTransformerMethod() {
    }

    public DataTransformerMethod(String name, String implementingClass){
        this();
        this.name = name;
        this.implementingClassName = implementingClass;
    }

    public long getId() {

        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImplementingClassName() {
        return implementingClassName;
    }

    public void setImplementingClassName(String implementingClassName) {
        this.implementingClassName = implementingClassName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataTransformerMethod that = (DataTransformerMethod) o;

        if (id != that.id) return false;
        if (implementingClassName != null ? !implementingClassName.equals(that.implementingClassName) : that.implementingClassName != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return visualizationMethod != null ? visualizationMethod.equals(that.visualizationMethod) : that.visualizationMethod == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (implementingClassName != null ? implementingClassName.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (visualizationMethod != null ? visualizationMethod.hashCode() : 0);
        return result;
    }
}
