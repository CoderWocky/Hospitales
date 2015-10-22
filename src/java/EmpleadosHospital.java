/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alumno
 */
@WebServlet(urlPatterns = {"/"})
public class EmpleadosHospital extends HttpServlet {
  
  private Connection conexionUnica;

  private String renderHeader() {
    return "<html>\n<head>\n<title>Hospitales</title>\n" +
        "<meta charset='UTF-8'>\n" +
        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
        "</head>\n";
  }

  private Connection obtenerConexion() throws SQLException {
    if (this.conexionUnica == null) {
      DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
      return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", 
          "system", "javaoracle");
    }
    return this.conexionUnica;
  }
  
  private String obtenerHospitales(String seleccionado) throws SQLException {
    Connection conexion = obtenerConexion();

    Statement sql = conexion.createStatement();
    ResultSet registros = sql.executeQuery("SELECT NOMBRE FROM HOSPITAL");

    String html = "<option disabled ";
    if (seleccionado.equals(""))
      html += "selected";
    html += ">Seleccione hospital</option>\n";
    while (registros.next()) {
      String nombre = registros.getString(1);
      html += String.format("<option value='%s'", nombre);
      if (nombre.equals(seleccionado))
        html += " selected";
      html += String.format(">%s</option>%n", nombre);
    }
    conexion.close();
    return html;
  }
  
  private String obtenerDoctores(String hospital) throws SQLException {
    Connection conexion = obtenerConexion();
    String query = "select apellido from doctor " +
        "inner join hospital on doctor.hospital_cod = hospital.hospital_cod " +
        "where hospital.nombre = ?";
    PreparedStatement sql = conexion.prepareStatement(query);
    sql.setString(1, hospital);
    ResultSet registros = sql.executeQuery();
    
    String html = "<table>\n";
    while (registros.next()) {
      html += String.format("<tr><td>%s</td></tr>%n", registros.getString(1));
    }
    return html + "</table>\n";
  }
  
  private String renderBody(String hospital) {
    // Si hospital es la cadena vacía, pinta la página inicial
    String html ="<body>\n<h1>Empleados del Hospital</h1>\n";
    html += "<form method='post'>";
    html += "<select name='hospital'>";
    try {
      html += obtenerHospitales(hospital);
    } catch (SQLException ex) {
      Logger.getLogger(EmpleadosHospital.class.getName()).log(Level.SEVERE, null, ex);
    }
    html += "</select>\n<input type='submit' value='Ver Doctores'>\n</form>\n";
    if (!hospital.equals(""))
      try {
        html += obtenerDoctores(hospital);
    } catch (SQLException ex) {
      Logger.getLogger(EmpleadosHospital.class.getName()).log(Level.SEVERE, null, ex);
    }
    html += "</body>\n</html>";
    return html;
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html");
    String html = renderHeader();
    html += renderBody("");
    response.getWriter().println(html);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    String hospital = request.getParameter("hospital");
    response.setContentType("text/html");
    String html = renderHeader();
    html += renderBody(hospital);
    response.getWriter().println(html);
  }
}
