import DAO.ReservaDAO;
import Model.ReservaDetalle;

import java.sql.SQLException;
import java.util.List;

public class Pruebas {

    public static ReservaDAO reservaDAO;

    public static void main(String[] args) {

        reservaDAO = new ReservaDAO();

        try {
            List<ReservaDetalle> detalles = reservaDAO.listarTodosDetalles();

            if (detalles != null && !detalles.isEmpty()){
                detalles.forEach(rs -> System.out.println(
                        rs.getIdReserva()+" "+rs.getDniCliente()+" "+rs.getNombreInstalacion()+" "+
                        rs.getTipoInstalacion()+" "+rs.getFechaReserva()+" "+rs.getHoraInicio()+" "+
                        rs.getHoraFin()+" "+rs.getPrecio()+" €"+rs.getEstado()));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
