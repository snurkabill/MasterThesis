package vahy.utils;

public abstract class NamedEntity {

    private final int id;
    private final String name;

    public NamedEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }




}
