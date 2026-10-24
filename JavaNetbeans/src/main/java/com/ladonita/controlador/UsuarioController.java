package com.ladonita.controlador;

import com.ladonita.modelo.dao.UsuarioDAO;
import com.ladonita.modelo.dao.impl.UsuarioDAOImpl;
import com.ladonita.modelo.entidades.Empleado;
import com.ladonita.modelo.entidades.Repartidor;
import com.ladonita.modelo.entidades.Rol;
import com.ladonita.modelo.entidades.Usuario;
import com.ladonita.util.Bitacora;
import com.ladonita.util.Contrasenas;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Gestión del personal (admin, empleados y repartidores). Solo administrador. */
@WebServlet(name = "UsuarioController", urlPatterns = {
    "/admin/usuarios", "/admin/usuarios/nuevo", "/admin/usuarios/editar",
    "/admin/usuarios/guardar", "/admin/usuarios/estado", "/admin/usuarios/clave"})
public class UsuarioController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN)) {
            return;
        }
        switch (req.getServletPath()) {
            case "/admin/usuarios/nuevo" -> formulario(req, resp, nuevoUsuario(Rol.EMPLEADO), List.of());
            case "/admin/usuarios/editar" -> {
                String id = req.getParameter("id");
                Usuario u = Validador.id(id) ? bd(cn -> new UsuarioDAOImpl(cn).buscarPorId(id)) : null;
                if (u == null) {
                    flash(req, "warning", "El usuario no existe.");
                    redirigir(req, resp, "/admin/usuarios");
                } else {
                    formulario(req, resp, u, List.of());
                }
            }
            default -> {
                req.setAttribute("usuarios", bd(cn -> new UsuarioDAOImpl(cn).listar()));
                vista(req, resp, "admin/usuarios");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN)) {
            return;
        }
        switch (req.getServletPath()) {
            case "/admin/usuarios/estado" -> cambiarEstado(req, resp);
            case "/admin/usuarios/clave" -> restablecerClave(req, resp);
            default -> guardar(req, resp);
        }
    }

    private void formulario(HttpServletRequest req, HttpServletResponse resp, Usuario u, List<String> errores)
            throws ServletException, IOException {
        req.setAttribute("usuario", u);
        req.setAttribute("errores", errores);
        req.setAttribute("roles", Rol.values());
        req.setAttribute("hoy", LocalDate.now().toString());
        vista(req, resp, "admin/usuarioForm");
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = Validador.vacio(req.getParameter("idUsuario")) ? null : req.getParameter("idUsuario");
        Rol rol;
        try {
            rol = Rol.valueOf(req.getParameter("rol"));
        } catch (RuntimeException e) {
            rol = Rol.EMPLEADO;
        }
        List<String> errores = new ArrayList<>();
        Usuario u = nuevoUsuario(rol);
        u.setIdUsuario(id);
        u.setNombres(Validador.limpiar(req.getParameter("nombres")));
        u.setApellidos(Validador.limpiar(req.getParameter("apellidos")));
        u.setDni(Validador.limpiar(req.getParameter("dni")));
        u.setUsuario(Validador.limpiar(req.getParameter("usuarioLogin")));
        u.setTelefono(Validador.limpiar(req.getParameter("telefono")));
        u.setCorreo(Validador.vacio(req.getParameter("correo")) ? null : req.getParameter("correo").trim());
        u.setEstado(true);

        if (!Validador.longitudValida(u.getNombres(), 100) || !Validador.longitudValida(u.getApellidos(), 100)) {
            errores.add("Nombres y apellidos son obligatorios.");
        }
        if (u.getDni() == null || !u.getDni().matches("\\d{8}")) {
            errores.add("El DNI debe tener 8 dígitos.");
        }
        if (u.getUsuario() == null || !u.getUsuario().matches("[A-Za-z0-9_.]{4,50}")) {
            errores.add("El usuario debe tener de 4 a 50 caracteres (letras, números, punto o guion bajo).");
        }
        if (!Validador.telefono(u.getTelefono())) {
            errores.add("El teléfono debe tener entre 7 y 15 dígitos.");
        }
        if (u.getCorreo() != null && !Validador.correo(u.getCorreo())) {
            errores.add("El correo no es válido.");
        }
        String clave = req.getParameter("clave");
        if (id == null) {
            String errorClave = Contrasenas.validarNueva(clave);
            if (errorClave != null) {
                errores.add(errorClave);
            }
        }
        leerDatosLaborales(req, u, errores);

        Usuario actual = usuarioActual(req);
        Usuario anterior = id == null ? null : bd(cn -> new UsuarioDAOImpl(cn).buscarPorId(id));
        if (id != null && anterior == null) {
            redirigir(req, resp, "/admin/usuarios");
            return;
        }
        if (anterior != null) {
            u.setEstado(anterior.isEstado());
            if (anterior.isAdmin() && rol != Rol.ADMIN && quedaSinAdmins(id)) {
                errores.add("Debe quedar al menos un administrador activo.");
            }
            if (id.equals(actual.getIdUsuario()) && rol != Rol.ADMIN) {
                errores.add("No puedes quitarte a ti mismo el rol de administrador.");
            }
        }
        if (errores.isEmpty()) {
            boolean[] duplicados = bd(cn -> {
                UsuarioDAO dao = new UsuarioDAOImpl(cn);
                return new boolean[]{dao.existeUsuario(u.getUsuario(), id), dao.existeDni(u.getDni(), id)};
            });
            if (duplicados[0]) {
                errores.add("Ese nombre de usuario ya está en uso.");
            }
            if (duplicados[1]) {
                errores.add("Ya existe un usuario con ese DNI.");
            }
        }
        if (!errores.isEmpty()) {
            formulario(req, resp, u, errores);
            return;
        }

        if (id == null) {
            u.setClaveHash(Contrasenas.hash(clave));
        }
        bdTransaccion(cn -> id == null ? new UsuarioDAOImpl(cn).insertar(u) : new UsuarioDAOImpl(cn).actualizar(u));
        Bitacora.info(req, id == null ? "USUARIO_CREADO" : "USUARIO_EDITADO",
                u.getIdUsuario() + " " + u.getUsuario() + " rol=" + u.getRol().getValorBD());
        flash(req, "success", "Usuario " + u.getUsuario() + (id == null ? " creado." : " actualizado."));
        redirigir(req, resp, "/admin/usuarios");
    }

    private void leerDatosLaborales(HttpServletRequest req, Usuario u, List<String> errores) {
        if (u.getRol() == Rol.ADMIN) {
            return;
        }
        LocalDate ingreso = null;
        try {
            ingreso = LocalDate.parse(req.getParameter(u.getRol() == Rol.EMPLEADO ? "fechaIngresoE" : "fechaIngresoR"));
            if (ingreso.isAfter(LocalDate.now())) {
                errores.add("La fecha de ingreso no puede ser futura.");
            }
        } catch (RuntimeException e) {
            errores.add("Indica la fecha de ingreso laboral.");
        }
        if (u instanceof Empleado e) {
            e.setFechaIngresoLaboral(ingreso);
            e.setTurno("tarde".equals(req.getParameter("turno")) ? "tarde" : "mañana");
            e.setAreaAsignada(Validador.limpiar(req.getParameter("area")));
            if (!Validador.longitudValida(e.getAreaAsignada(), 50)) {
                errores.add("Indica el área asignada (máx. 50 caracteres).");
            }
            try {
                BigDecimal sueldo = new BigDecimal(req.getParameter("sueldo").trim().replace(',', '.'));
                if (sueldo.signum() < 0 || sueldo.compareTo(new BigDecimal("999999.99")) > 0) {
                    throw new NumberFormatException();
                }
                e.setSueldo(sueldo);
            } catch (RuntimeException ex) {
                errores.add("El sueldo debe ser un número positivo.");
            }
        } else if (u instanceof Repartidor r) {
            r.setFechaIngresoLaboral(ingreso);
            r.setPlacaVehiculo(Validador.limpiar(req.getParameter("placa")));
            String tipo = req.getParameter("tipoVehiculo");
            r.setTipoVehiculo("auto".equals(tipo) || "bicicleta".equals(tipo) ? tipo : "moto");
            r.setZonaReparto(Validador.vacio(req.getParameter("zona")) ? null : req.getParameter("zona").trim());
            r.setLicencia(Validador.vacio(req.getParameter("licencia")) ? null : req.getParameter("licencia").trim());
            if (!Validador.longitudValida(r.getPlacaVehiculo(), 10)) {
                errores.add("Indica la placa del vehículo (máx. 10 caracteres).");
            }
            if (r.getZonaReparto() != null && r.getZonaReparto().length() > 100
                    || r.getLicencia() != null && r.getLicencia().length() > 20) {
                errores.add("Zona (máx. 100) o licencia (máx. 20) demasiado largas.");
            }
        }
    }

    private void cambiarEstado(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        boolean activar = "true".equals(req.getParameter("activar"));
        if (!Validador.id(id)) {
            redirigir(req, resp, "/admin/usuarios");
            return;
        }
        Usuario u = bd(cn -> new UsuarioDAOImpl(cn).buscarPorId(id));
        if (u == null) {
            flash(req, "warning", "El usuario no existe.");
        } else if (!activar && id.equals(usuarioActual(req).getIdUsuario())) {
            flash(req, "danger", "No puedes desactivar tu propia cuenta.");
        } else if (!activar && u.isAdmin() && quedaSinAdmins(id)) {
            flash(req, "danger", "Debe quedar al menos un administrador activo.");
        } else {
            bd(cn -> new UsuarioDAOImpl(cn).cambiarEstado(id, activar));
            Bitacora.info(req, activar ? "USUARIO_ACTIVADO" : "USUARIO_DESACTIVADO", id + " " + u.getUsuario());
            flash(req, "success", "Usuario " + u.getUsuario() + (activar ? " activado." : " desactivado: ya no podrá iniciar sesión."));
        }
        redirigir(req, resp, "/admin/usuarios");
    }

    private void restablecerClave(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String clave = req.getParameter("clave");
        String error = Contrasenas.validarNueva(clave);
        if (!Validador.id(id)) {
            redirigir(req, resp, "/admin/usuarios");
            return;
        }
        if (error != null) {
            flash(req, "danger", error);
        } else {
            bd(cn -> new UsuarioDAOImpl(cn).cambiarClave(id, Contrasenas.hash(clave)));
            Bitacora.info(req, "CLAVE_RESTABLECIDA", id);
            flash(req, "success", "Contraseña restablecida. Comunícala al usuario por un medio seguro.");
        }
        redirigir(req, resp, "/admin/usuarios/editar?id=" + id);
    }

    private boolean quedaSinAdmins(String idExcluido) throws ServletException {
        return bd(cn -> new UsuarioDAOImpl(cn).listarPorRol(Rol.ADMIN).stream()
                .noneMatch(a -> !a.getIdUsuario().equals(idExcluido)));
    }

    private Usuario nuevoUsuario(Rol rol) {
        Usuario u = switch (rol) {
            case EMPLEADO -> new Empleado();
            case REPARTIDOR -> new Repartidor();
            default -> new Usuario();
        };
        u.setRol(rol);
        u.setEstado(true);
        return u;
    }
}
