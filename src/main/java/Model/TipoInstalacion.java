package Model;

public enum TipoInstalacion {
    BAR("Bar"),
    SALON_EVENTOS("Salon de eventos"),
    PISCINA("Piscina"),
    GIMNASIO("Gimnasio"),
    PADEL("Padel"),
    BARBACOA("Barbacoa");

    // Atributo para mostrar en la vista del usuario
    private final String nombreInstalacion;

    // Constructor del Enum
    TipoInstalacion(String nombreInstalacion){
        this.nombreInstalacion = nombreInstalacion;
    }

    // Para mostrar en la vista
    public String getNombreInstalacion(){
        return nombreInstalacion;
    }
}
