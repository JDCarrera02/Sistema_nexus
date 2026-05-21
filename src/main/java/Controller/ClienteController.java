package Controller;

import DAO.ClienteDAO;
import Model.Cliente;
import Util.Login;
import Util.MensajeSQL;
import Util.Validator;

import View.VistaCliente;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Clase ClienteController
 * En esta clase se llamarán los metodos del ClienteDAO, haciendo las validaciones necesarias para poder construir los objetos
 * de manera correcta, y hacer inserciones, modificaciones, consultas y eliminaciones.
 * Tiene dos atributos privados de tipo ClienteDAO, para usar los metodos respectivos, y otro que se utilizará para la vista, y llamar directamente los metodos para mostrar mensajes respectivos al usuario
 */
public class ClienteController {
    private final ClienteDAO clienteDAO;
    private final VistaCliente vista;

    public ClienteController(VistaCliente vista) {
        this.clienteDAO = new ClienteDAO();
        this.vista = vista;
    }

    /**
     * Metodo que gestiona validaciones de atributos para la creacion de un cliente
     *
     * @param dni             el dni del cliente a crear
     * @param nombre          el nombre del cliente
     * @param apellidos       el o los apellidos del cliente
     * @param email           el correo electronico del cliente
     * @param telefono        el numero de telefono del cliente (atributo opcional, pero cuando hay informacion, se valida su formato)
     * @param fechaNacimiento la fecha de nacimiento del cliente, (este atributo es opcional, por eso no se comprueba nada, ya que el formato lo maneja el propio LocalDate)
     * @return si se hace la insercion o no, este valor será utilizado en las vistas para mostrar los mensajes al usuario
     */
    public boolean insertarCliente(String dni, String nombre, String apellidos, String email, String telefono, LocalDate fechaNacimiento) {

        // Primeros pasos antes de insertar un cliente, validacion de campos a partir de la clase Validator. Todo con el fin de crear un objeto con datos validos
        try {
            Validator.validarDni(dni); // Validacion del dni, que no sea null, y que cumpla con el patron Regex definido
            Validator.validarNombreCompleto(nombre, "Nombre"); // Validacion del nombre, que no sea null, y que solamente contenga letras
            Validator.validarNombreCompleto(apellidos, "Apellidos"); // Validacion del apellido, que no sea null y que solamente contenga letras
            Validator.validarEmail(email); // Validacion del email, que no sea null y que cumpla con el formato definido
            Validator.validarTelefono(telefono); // Validacion del telefono, que solamente tenga numeros (pueden estar separados por guiones o no)
        } catch (IllegalArgumentException e) {
            // Capturar error de validacion
            vista.mostrarError(e.getMessage());
            return false; // Algun parametro es erroneo, no se puede insertar
        }

        try {
            // Si los datos de entrada son validos, se crea el objeto
            Cliente cliente = new Cliente(dni, nombre, apellidos, email, telefono, fechaNacimiento);

            clienteDAO.insertar(cliente); // Llamar al metodo del DAO para insertar en la base de datos el cliente
            Login.log("Cliente registrado -- DNI: " + dni);
            vista.mostrarMensaje("Cliente registrado correctamente"); // Mostrar mensaje en la vista para el usuario, informandole que se ha insertado correctamente el cliente
            return true; // Se ha realizado la insercion correctamente

        } catch (SQLException e) {
            Login.error("Error al insertar cliente: " + e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e, "DNI o email")); // Mostrar mensaje de error si existe algun error con la base de datos
            return false; // Hubo un error en la base de datos, no se inserta
        }
    }

    /**
     * Metodo que llama al metodo del dao para actualizar clientes.
     * Se validan todas las entradas, que no sean nulas o que cumplan con el patron definido en la clase Util.Validator,
     * si alguno de los parametros (obligatorios, definidos en el modelo relacional) no cumple con lo requerido, no se continua, se lanza una excepcion y se muestra en una ventana de mensaje en la vista, informando al usuario la causa de error y qué parametro no cumple con los requisitos.
     * Luego de las validaciones, se pasa a buscar al cliente que se desea actualizar, se llama al metodo del dao que retorna el resultado y se guarda en un objeto Cliente,
     * se comprueba si el objeto contiene informacion, si en null, se muestra el mensaje respectivo en la vista y se sale del procedimiento; en caso de que no lo sea,
     * por medio de los setters del objeto, se actualiza la informacion del objeto (con informacion), se pasa ese objeto completo como parametro a la funcion
     * actualizar() y si se hace correctamente, se muestra el mensaje de exito en la vista.
     * Si ocurre algun error, se captura en el catch y por medio del metodo privado de esta clase, se pasa el error y se traduce en un mensaje legible para el usuario y mostrarlo en la vista para informar lo que ha sucedido.
     *
     * @param dni             el dni del cliente a actualizar
     * @param nombre          el nombre del cliente actuqalizado
     * @param apellidos       el o los apellidos del cliente actualizado
     * @param email           el correo electronico del cliente actualizado
     * @param telefono        el numero de telefono del cliente (atributo opcional, pero cuando hay informacion, se valida su formato)
     * @param fechaNacimiento la fecha de nacimiento del cliente, (este atributo es opcional, por eso no se comprueba nada, ya que el formato lo maneja el propio LocalDate)
     * @return un booleano, para indicar si se ha actualizado el cliente o no
     */
    public boolean actualizarCliente(String dni, String nombre, String apellidos, String email, String telefono, LocalDate fechaNacimiento) {

        // Validacion de entradas
        try {
            Validator.validarDni(dni);
            Validator.validarNombreCompleto(nombre, "Nombre");
            Validator.validarNombreCompleto(apellidos, "Apellidos");
            Validator.validarEmail(email);
            Validator.validarTelefono(telefono);

        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage()); // Capturar excepcion y mostrar al usuario en la vista
            return false;
        }

        // Verificar que el cliente existe antes de actualizarlo
        try {
            Cliente cliente = clienteDAO.buscarPorId(dni);

            if (cliente == null) {
                vista.mostrarError("No existe ningun cliente con DNI: " + dni);
                return false; // Si no existe el cliente no se actualiza
            }

            // Actualizar solo los campos modificables por medio de los setters del objeto Cliente
            cliente.setNombre(nombre);
            cliente.setApellidos(apellidos);
            cliente.setEmail(email);
            cliente.setTelefono(telefono);
            cliente.setFechaNacimiento(fechaNacimiento);

            clienteDAO.actualizar(cliente);
            vista.mostrarMensaje("Cliente actualizado correctamente");
            Login.log("Cliente actualizado -- DNI: " + dni);
            return true; // Se ha actualizado el cliente correctamente

        } catch (SQLException e) {
            Login.error("Error al actualizar el cliente: " + e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e, "email"));
            return false; // No se actualiza el cliente
        }
    }

    /**
     * Metodo que llama al metodo del dao para eliminar un cliente a partir de su dni.
     * Valida que el dni cumpla con el patron definido en la clase Util.Validator, si no lo cumple, se muestra mensaje de error en la vista y se retorna null.
     * Siguiente paso, al cumplir con la validacion, se llama al metodo del dao para buscar el cliente primero (comprobar si existe) y el resultado obtenido se guarda en un objeto Cliente,
     * si no encuentra nada, se muestra el mensaje en la vista y se sale del procedimiento; si lo encuentra, llama al metodo de eliminar del dao y muestra el mensaje de que se ha eliminado el cliente correctamente
     *
     * @param dni el dni del cliente a eliminar (se comprueba primero que el cliente exista primero antes de eliminar)
     * @return si se realiza la eliminacion del cliente o no.
     */
    public boolean eliminar(String dni) {
        // Validar entrada (DNI)
        try {
            Validator.validarDni(dni);
        } catch (IllegalArgumentException e) {
            // Si el parametro de entrada no cumple con los requisitos (que no sea null, y que cumpla con el formato Regex definido)
            vista.mostrarError(e.getMessage()); // Mostrar en la vista el mensaje de error
            return false; // Si el parametro de entrada no es valido, no se elimina el cliente
        }

        // Verificacion de que existe el cliente en la base de datos antes de eliminarlo
        try {
            // Crear el objeto cliente a partir del registro encontrado de la base de datos, a partir del metodo del DAO
            Cliente cliente = clienteDAO.buscarPorId(dni);

            // Si el objeto es null (no lo ha encontrado)
            if (cliente == null) {
                vista.mostrarError("No existe ningún cliente con DNI: " + dni); // Se muestra mensaje en la vista al usuario
                return false; // No se elimina un cliente que no existe
            }

            // Si lo encuentra, se llama al metodo del DAO para eliminar el cliente utilizando el dni como parametro de busqueda
            clienteDAO.eliminar(dni);
            Login.log("Cliente eliminado -- DNI: " + dni);
            vista.mostrarMensaje("Cliente eliminado correctamente."); // Al eliminarse, se muestra en la vista el mensaje de confirmacion
            return true; // Cliente eliminado

        } catch (SQLException e) {
            Login.error("Error al eliminar el cliente: " + e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e, "cliente")); // Si ocurre algun error con la base de datos durante el procedimiento de eliminacion, se muestra mensaje de error al usuario en la vista
            return false; // No se elimina si hay algun error con la base de datos
        }
    }

    /**
     * Metodo que llama a la funcion del dao, para buscar un cliente por un dni de entrada.
     * El metodo valida que el dni de entrada, cumpla con el patron o "REGEX" definido en la clase Util.Validator,
     * si no lo cumple o la entrada está vacia, la funcion retorna null.
     * Después de la validacion, llama al metodo del dao, y de ahí se guarda el resultado en un objeto Cliente, luego se valida si el objeto es null (no hay resultado),
     * si lo es, la función muestra en la vista el mensaje respectivo y retorna el objeto.
     * Si hay algun error con la base de datos, la función retornará null.
     *
     * @return El objeto cliente a partir del registro encontrado por el dni especificado como parametro de busqueda (si es null, no lo ha encontrado)
     */
    public Cliente buscarPorDni(String dni) {
        try {
            // Validar entrada
            Validator.validarDni(dni);
        } catch (IllegalArgumentException e) {
            // Si no cumple con los requisitos
            vista.mostrarError(e.getMessage()); // Se muestra mensaje de error
            return null; // Retorno de null
        }

        try {
            // Crear objeto cliente a partir del resultado de busqueda a traves del metodo del DAO
            Cliente cliente = clienteDAO.buscarPorId(dni);

            // Si es null el objeto retornado (Sin resultados)
            if (cliente == null) {
                vista.mostrarError("No existe ningún cliente con DNI: " + dni); // Mostrar mensaje de error en la vista
            }

            return cliente; // Si hay contenido, retorna el objeto con el respectivo registro encontrado en la base de datos

        } catch (SQLException e) {
            Login.error("Error al buscar cliente: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    /**
     * Metodo que llama al metodo del dao para recuperar un cliente a partir de su email
     * Valida que la entrada no sea nula, o que cumpla con el patron "REGEX" definido en la clase Validator.
     * Llama al metodo del dao, el resultado se guarda en un objeto Cliente, se valida si es null (no hay resultado), si lo es, muestra el mensaje en la vista
     * si no, retorna el cliente encontrado a partir del email especificado
     *
     * @param email el email del cliente a buscar
     * @return el objeto Cliente (resultado de la busqueda) o null si el email no es valido, o no encuentra un cliente con el email de entrada
     */
    public Cliente buscarPorEmail(String email) {
        try {
            // Validar entrada
            Validator.validarEmail(email);
        } catch (IllegalArgumentException e) {
            // Si no cumple con los requisitos (de formato y contenido)
            vista.mostrarError(e.getMessage()); // Mostrar mensaje en la vista
            return null; // Retorno de null
        }

        try {
            // Crear objeto Cliente a partir de los resultados de retorno del metodo del DAO
            Cliente cliente = clienteDAO.buscarPorEmail(email);

            // Si el objeto es null (no hay resultados)
            if (cliente == null) {
                vista.mostrarError("No existe ningún cliente con email: " + email); // Mostrar mensaje de error respectivo al usuario en la vista
            }

            return cliente; // Si lo encuentra, retorna el objeto con el registro encontrado en la base de datos

        } catch (SQLException e) {
            Login.error("Error al buscar cliente por email: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    /**
     * Metodo que llama al metodo del DAO para listar los clientes
     *
     * @return La lista con los resultados encontrados
     * Si hay algun error con la base de datos, captura la excepcion lanzada en el metodo del dao, y la muestra en la vista
     */
    public List<Cliente> listarTodos() {

        try {
            // Preparar lista de retorno a partir de los resultados a traves del metodo del DAO
            return clienteDAO.listarTodos();

        } catch (SQLException e) {
            Login.error("Error al buscarr clientes: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    /**
     * Metodo controlador que llama al metodo del DAO, buscarTermino() para recuperar una lista de clientes
     * por medio de criterio de busqueda: nombre, dni, apellidos
     *
     * @param termino el criterio de busqueda
     * @return La lista de clientes encontrados o null si hay alguna excepcion o devuelve todos los clientes si no hay resultados con el termino de busqueda especificado
     */
    public List<Cliente> buscarPorTermino(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodos();
        }

        try {
            return clienteDAO.buscarPorTermino(termino);
        } catch (SQLException e) {
            Login.error("Error al buscar clientes por termino: "+termino+" ==== "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }
}
