package de.createplus.vertretungsplan;


public enum ContentViews {
    OVERVIEW(R.layout.content_overview),
    TIMETABLE(R.layout.content_timetable),
    SUBSTITUTIONPLAN(R.layout.content_substitutionplan),
    ADVERTISMENT(R.layout.content_advertisment);

    private int id;

    private ContentViews(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
