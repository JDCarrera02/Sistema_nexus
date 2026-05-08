package Model;

public enum TipoMembresia {
    ESSENTIAL("Plan Essential"),
    VIP("Plan VIP"),
    PREMIUM("Plan Premium");

    // Atributo para mostrar en la vista del usuario
    private final String nombreMembresia;

    // Constructor del Enum
    TipoMembresia(String nombreMembresia){
        this.nombreMembresia = nombreMembresia;
    }

    // Para mostrar en la vista
    public String getNombreMembresia(){
        return nombreMembresia;
    }
}
