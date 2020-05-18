package ro.pub.cs.systems.eim.practicaltest02.model;

public class CurrencyInformation {
    private String updated;
    private String code;
    private String rate;
    private String description;
    private String rate_float;

    public CurrencyInformation() {
        this.code = null;
        this.rate = null;
        this.description = null;
        this.rate_float = null;
        this.updated = null;
    }

    public CurrencyInformation(String code, String rate, String description, String rate_float, String updated) {
        this.code = code;
        this.rate = rate;
        this.description = description;
        this.rate_float = rate_float;
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "CurrencyInformation{" +
                "code='" + code + '\'' +
                ", rate='" + rate + '\'' +
                ", description='" + description + '\'' +
                ", rate_float='" + rate_float + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }

    public String getUpdated() {
        return updated;
    }
}
