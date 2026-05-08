package Model;

public enum EstadoReserva {
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada"),
    COMPLETADA("Completada");

    private final String nombreVisible;

    EstadoReserva (String nombreVisible){
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible(){
        return nombreVisible;
    }
}
