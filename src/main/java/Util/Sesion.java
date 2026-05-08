package Util;

import Model.Socio;

public class Sesion {

    // Instancia unica
    private static Sesion instancia;

    // Atributos de la sesion
    private String dniUsuario;
    private Rol rol;
    private Socio socio;

    // Constructor privado para que esta clase no pueda instanciarse, es una clase estatica
    private Sesion(){}

    // Getter para obetener la sesion
    public static Sesion getInstancia(){
        if (instancia == null){ // Si la instancia es null, se instancia la Sesion internamente para usar sus metodos
            instancia = new Sesion();
        }

        return instancia;
    }

    // Iniciar sesion como admin
    public void iniciarComoAdmin(){
        this.dniUsuario = "admin";
        this.rol = Rol.ADMIN;
        this.socio = null;
    }

    // Iniciar sesion como socio
    public void iniciarComoSocio(String dni, Socio socio){
        this.dniUsuario = dni;
        this.rol = Rol.SOCIO;
        this.socio = socio;
    }

    // Iniciar sesion como cliente no socio
    public void iniciarComoCliente(String dni){
        this.dniUsuario = dni;
        this.rol = Rol.CLIENTE;
        this.socio = null;
    }

    // Cerrar sesion actual
    public void cerrarSesion(){
        this.dniUsuario = null;
        this.rol = null;
        this.socio = null;
    }

    // Getters

    public String getDniUsuario() {
        return dniUsuario;
    }

    public Rol getRol() {
        return rol;
    }

    public Socio getSocio() {
        return socio;
    }

    // Para comprobar roles

    public boolean esAdmin(){
        return rol == Rol.ADMIN;
    }

    public boolean esSocio(){
        return rol == Rol.SOCIO;
    }

    public boolean esCliente(){
        return rol == Rol.CLIENTE;
    }
}
