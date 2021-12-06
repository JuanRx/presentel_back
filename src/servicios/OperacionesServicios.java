package servicios;

import java.sql.Timestamp;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.covilib.logs.CoviLog;
import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
//import com.covilib.orm.implementaciones.ModeloMysql;
import com.covilib.orm.implementaciones.ModeloSQLite;
import com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro;
//import com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.Formato;
//import com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.TipoDato;
import com.covilib.reflection.annotations.propiedades_servicio.PropiedadesApi;
import com.covilib.reflection.annotations.propiedades_servicio.PropiedadesServicio;
import com.covilib.security.JWT;
import com.covilib.util.CoviFecha;
import com.covilib.util.CoviLibUtils;
//import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadesServicio.TipoAcceso.PUBLICO;
import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadesServicio.Metodo.POST;
import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.TipoDato.STRING;
import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.TipoIn.BODY;
//import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.TipoDato.INTEGER;
import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.Formato.CORREO;
//import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.Formato.TELEFONO;
//import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.TipoDato.FILE;
//import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.TipoDato.LISTA;
//import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.TipoIn.FORM_DATA;
import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadParametro.TipoIn.QUERY;
import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadesServicio.Metodo.GET;
import static com.covilib.reflection.annotations.propiedades_servicio.PropiedadesServicio.TipoAcceso.PRIVADO;

import entities.Asistencia;
import entities.Ficha;
import entities.Notificaciones;
import entities.Rol;
import entities.Tematica;
import entities.Usuario;
import entities.UsuarioFicha;
import enums.EstadoUsuario;

@PropiedadesApi(nombreProyecto = "ASISTENCIA", rutaBase = "/asistencia/servicios/", descripcion = "Proyecto control de asistencia", secretoDEV = "a7306861-21f7-49s2-b12a-1285591e1310", secretoPROD = "238d8d20-238e-4f0-bc53-106181714208", secretoQA = "d37fe360-7005-4b0g-8a85-1140d5ef833c")
public class OperacionesServicios extends com.covilib.servicios.CoviOperacionesServicios {

	public ModeloOrm modelo;

	public static String STR_CONEXION_SQLITE = null;// "java:/etec-ds";

	public OperacionesServicios() {
		super();

		if (STR_CONEXION_SQLITE == null) {
			STR_CONEXION_SQLITE = getConfig().getData("configuracion.ruta_config");
			STR_CONEXION_SQLITE += "pdw.db";
		}

		modelo = new ModeloSQLite(STR_CONEXION_SQLITE);

	}

	@Override
	public String servicio_status(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
		return null;
	}

	@Override
	public boolean tienePermisos(String arg0, String arg1) throws Exception {
		return true;
	}

	@PropiedadesServicio(metodo = POST, 
			acceso = PUBLICO, 
			descripcion = "Hola mundo", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "nombre", description = "Nombre a saludar", required = true, type = STRING), 
			})
	public String servicio_hola_mundo(HttpServletRequest request, HttpServletResponse response) throws Exception {

		JSONObject result = new JSONObject();

		result.put("respuesta", "Hola " + getDatoEntrada("nombre", request));

		return "" + result;

	}

	@PropiedadesServicio(metodo = POST, 
			acceso = PUBLICO, 
			descripcion = "Inicio de sesion de todos los usuarios", 
			parametros = {
			// @PropiedadParametro(in = BODY, name = "id_centro", description = "Id del
			// centro de costo", required = true, type = INTEGER),
			@PropiedadParametro(in = BODY, name = "user", description = "Nombre de usaurio", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "password", description = "Contraseña del usuario", required = true, type = STRING) 
			})
	public String servicio_login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject result = null;
		try {
			String vUser = getDatoEntrada("user", request);
			String vPassword = getDatoEntrada("password", request);

			Usuario validador = new Usuario(modelo);
			validador = (Usuario) validador.buscarElemento(" where nick = '" + vUser + "' ");
			if (validador == null) {
				return getError(HttpServletResponse.SC_NOT_FOUND, "Este usuario no existe", response);
			}
			
			Usuario validadorEstado = new Usuario(modelo);
			validadorEstado = (Usuario) validadorEstado.buscarElemento(" where nick = '" + vUser + "' and estado <> 'H'");
			if (validadorEstado != null) {
				return getError(HttpServletResponse.SC_FOUND, "Este usuario no esta activo", response);
			}
			
			Usuario validadorRol = new Usuario(modelo);
			validadorRol = (Usuario) validadorRol.buscarElemento(" where nick = '" + vUser + "' and id_rol_fk in ('ROL-58ce1734','ROL-becf9471','ROL-900ed58f','ROL-697e0579')");
			if (validadorRol == null) {
				return getError(HttpServletResponse.SC_FOUND, "Este usuario no tiene un rol valido", response);
			}
			

			String contrasenaEncriptada = validador.generarContrasena(vPassword);

			// String dbPass = Usuario.generarContrasena(modelo, vUser, vPassword);

			String sql = "select * from " + ObjectOrm.obtenerNombreTabla(Usuario.class) + " where ";
			sql += " `contrasena` = '" + contrasenaEncriptada + "' ";
			sql += " AND `nick` = '" + vUser + "'";
			sql += " AND estado = '" + EstadoUsuario.HABILITADO + "'";

			CoviLog.escribirLogInfo(getClass(), sql);

			JSONArray consulta = modelo.executeQuery(sql);
			if (consulta.length() > 0) {

				result = consulta.getJSONObject(0);
				result.remove("contrasena");
				String token = generarToken(vUser, result);

				CoviLibUtils.setJson(result, "TOKEN", token);
				CoviLibUtils.setJson(result, "ahora", CoviFecha.ahora());
				Timestamp expiration = new Timestamp(JWT.getExpiration(token, getSecret()).getTime());
				CoviLibUtils.setJson(result, "EXPIRA", ("" + expiration).substring(0, 19));

			} else {
				return getError(HttpServletResponse.SC_UNAUTHORIZED, "Error de seguridad", response);
			}

		} catch (Exception e) {
			return getError(HttpServletResponse.SC_UNAUTHORIZED, "Error de seguridad", response);
		}
		return "" + result;
	}

	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para crear un nuevo usuario", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "nombres", description = "Nombres del usuario", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "apellidos", description = "Apellidos del usuario", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "tipo_doc", description = "id del tipo documento", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "documento", description = "numero documento", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idRol", description = "id del rol", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "nick", description = "Nick del usuario", required = true, type = STRING, format = CORREO)
			})
	public String servicio_save_usuario(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vNombres = getDatoEntrada("nombres", request);
		String vApellidos = getDatoEntrada("apellidos", request);
		String vNick = getDatoEntrada("nick", request);
		String vIdRol = getDatoEntrada("idRol", request);
		String vTipoDoc = getDatoEntrada("tipo_doc", request);
		String vDocumento = getDatoEntrada("documento", request);
		String vContrasena = generarContrasena(10);
		String[] tipoDoc = {"C","T","E","P"};
		int insertado = 0;
		int pos = 0;
		int campos = 0;
		JSONObject result = new JSONObject();
		
		vTipoDoc.toUpperCase();
		
		while (campos == 0) {
			while (tipoDoc[pos].equals(vTipoDoc.toUpperCase())) {
				Usuario validador = new Usuario(modelo);
				validador = (Usuario) validador.buscarElemento(" where nick = '" + vNick + "' ");
				if (validador != null) {
					return getError(HttpServletResponse.SC_FOUND, "Este correo ya esta registrado", response);
				}

				Usuario us = new Usuario(modelo);
				us.setNombres(vNombres);
				us.setApellidos(vApellidos);
				us.setNick(vNick);
				us.setEstado("" + EstadoUsuario.HABILITADO);
				us.setIdRolFk(vIdRol);
				us.setTipoDoc(vTipoDoc.toUpperCase());
				us.setDocumento(vDocumento);
				us.setFechaCreacion("" + CoviFecha.ahora());
				us.setContrasena(vContrasena);
				us.generarContrasena(vContrasena);

				us.guardar();
				
				result = us.toJson();

				result.remove("contrasena");
				insertado = 1;

				return "" + result;
				
				
				
			}
			pos++;
			if (pos == 4) {
			campos++;	
			}
			
		}
		
		if (insertado == 0) {
			return getError(HttpServletResponse.SC_NOT_FOUND, "Este tipo documento no es valido", response);
		}
		return "" + result;

	}

	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para actualizar un usuario", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "nombres", description = "Nombres del usuario", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "nick", description = "Nick del usuario", required = true, type = STRING, format = CORREO),
			@PropiedadParametro(in = BODY, name = "idUsuario", description = "id del usuario", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "apellidos", description = "apellidos del usuario", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "documento", description = "documento del usuario", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "tipoDoc", description = "tipo documento del usuario", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "estado", description = "Estado del usuario en el sistema", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "id_rol_fk", description = "id rol del usuario", required = true, type = STRING)
			})
	public String servicio_update_usuario(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vIdUsuario = getDatoEntrada("idUsuario", request);
		String vNombres = getDatoEntrada("nombres", request);
		String vNick = getDatoEntrada("nick", request);
		String vApellidos = getDatoEntrada("apellidos", request);
		String vDocumento = getDatoEntrada("documento", request);
		String vTipoDoc = getDatoEntrada("tipoDoc", request);
		String vEstado = getDatoEntrada("estado", request);
		String vIdRol = getDatoEntrada("id_rol_fk", request);

		Usuario validador = new Usuario(modelo);
		validador = (Usuario) validador.buscarElemento(" where id_usuario = '" + vIdUsuario + "' ");
		if (validador == null) {
			return getError(HttpServletResponse.SC_NOT_FOUND, "El usuario no esta registrado", response);
		}

		Usuario us = validador;
		us.setNombres(vNombres);
		us.setNick(vNick);
		us.setApellidos(vApellidos);
		us.setDocumento(vDocumento);
		us.setTipoDoc(vTipoDoc);
		us.setIdRolFk(vIdRol);
		us.setEstado(vEstado);
		
		us.actualizar(" where id_usuario = '" + vIdUsuario + "' ");

		JSONObject result = us.toJson();

		result.remove("contrasena");
		result.remove("id_usuario");

		return "" + result;

	}

	

	

	@PropiedadesServicio(metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Obtener las acciones del menu de usuarios"
			)
	public String servicio_acciones_usuario(HttpServletRequest request, HttpServletResponse response) throws Exception {

		JSONArray result = new JSONArray();
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLogeado = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
		
		Rol validaAdmin = new Rol(modelo);
		validaAdmin = (Rol)validaAdmin.buscarElemento(" where id_rol = 'ROL-58ce1734'");
		
		Rol validaInstructor = new Rol(modelo);
		validaInstructor = (Rol)validaInstructor.buscarElemento(" where id_rol = 'ROL-becf9471'");
		
		Rol validaAprendiz = new Rol(modelo);
		validaAprendiz = (Rol)validaAprendiz.buscarElemento(" where id_rol = 'ROL-900ed58f'");
		
		Rol validaCoordinador = new Rol(modelo);
		validaCoordinador = (Rol)validaCoordinador.buscarElemento(" where id_rol = 'ROL-697e0579'");
		
		Usuario validadorUsuario = new Usuario(modelo);
		validadorUsuario = (Usuario) validadorUsuario.buscarElemento(" where id_usuario = '" + vIdUsuarioLogeado + "' ");
		
		Rol validadorRol = new Rol(modelo);
		validadorRol = (Rol)validadorRol.buscarElemento(" where id_rol = '" + validadorUsuario.getIdRolFk() + "'");
		
		if (validadorRol.getIdRol().equals(validaAdmin.getIdRol())) {
			result.put(getAccion(" Modificar Usuarios", "fas fa-user-edit", "modificarUs.html"));
			result.put(getAccion(" Carga Masiva Usuarios", "fas fa-address-book", "cargaUsuarios.html"));
			result.put(getAccion(" Asignación de Fichas", "fas fa-clipboard-check", "asignarFichas.html"));
			result.put(getAccion(" Tematica", "fas fa-puzzle-piece", "tematicas.html"));
			
			return "" + result;
		}
		
		if (validadorRol.getIdRol().equals(validaInstructor.getIdRol())) {
			result.put(getAccion("Registrar Asistencia", "far fa-clipboard", "registrar.html"));
			result.put(getAccion("Consultar Asistencia", "fas fa-search", "consultar.html"));
			result.put(getAccion("Notificar Aprendiz", "far fa-bell", "notificar.html"));
			result.put(getAccion("Validar Excusas", "fas fa-address-book", "excusas.html"));
			result.put(getAccion("Generar Estadisticas", "fas fa-chart-line", "estadisticasIns.html"));
			return "" + result;
		}
		
		if (validadorRol.getIdRol().equals(validaAprendiz.getIdRol())) {
			result.put(getAccion("Consultar Asistencia", "fas fa-search", "aprendizAsistencia.html"));
			return "" + result;
		}
		
		if (validadorRol.getIdRol().equals(validaCoordinador.getIdRol())) {
			result.put(getAccion("Generar", "fas fa-desktop", "generar.html"));
			return "" + result;
		}
		return "" + result;
	}

	public static JSONObject getAccion(String label, String icono, String pagina) {
		JSONObject result = new JSONObject();
		CoviLibUtils.setJson(result, "label", label);
		CoviLibUtils.setJson(result, "icono", icono);
		CoviLibUtils.setJson(result, "pagina", pagina);
		return result;
	}
	
	
@PropiedadesServicio(
		metodo = POST, 
		acceso = PRIVADO, 
		descripcion = "Servicio para borrar un usuario",
		parametros = {
				@PropiedadParametro(in = BODY, name = "id_usuario", description = "id del usuario", required = true, type = STRING)
	})
	public String servicio_remove_usuario(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String vIdUsuario = getDatoEntrada("id_usuario", request);
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLogeado = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
		
		Usuario validador = new Usuario(modelo);
		validador = (Usuario) validador.buscarElemento(" where id_usuario = '" + vIdUsuario + "' ");
		if (validador == null) {
			return getError(HttpServletResponse.SC_NOT_FOUND, "Este usuario no esta registrado", response);
		}
		if (vIdUsuario.equals(vIdUsuarioLogeado)) {
			return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No puedes borrar tu propio usuario", response);
		}
		
		JSONObject result = new JSONObject();
		
		String sql = "delete from tbl_usuario where id_usuario= '" + vIdUsuario + "'";
		boolean resultado = modelo.actualizarTodo(sql);
		result.put("resultado_borrado_usuario", resultado); 
		
		sql = "delete from tbl_contrasena where id_usuario_fk= '" + vIdUsuario + "'";
		resultado = modelo.actualizarTodo(sql);
		result.put("resultado_borrado_contrasenas", resultado); 
		
		return "" + result;
	

}

@PropiedadesServicio(
		metodo = POST, 
		acceso = PRIVADO, 
		descripcion = "Servicio para modificar un usuario",
		parametros = {
				@PropiedadParametro(in = BODY, name = "id_usuario", description = "id del usuario", required = true, type = STRING),
				@PropiedadParametro(in = BODY, name = "nombres", description = "nombre del usuario", required = true, type = STRING),
				@PropiedadParametro(in = BODY, name = "nick", description = "Correo del usuario", required = true, type = STRING)
	})
	public String servicio_modify_usuario(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String vIdUsuario = getDatoEntrada("id_usuario", request);
		String vNombres = getDatoEntrada("nombres", request);
		String vNick = getDatoEntrada("nick", request);
		
		Usuario validador = new Usuario(modelo);
		validador = (Usuario) validador.buscarElemento(" where id_usuario = '" + vIdUsuario + "' ");
		if (validador == null) {
			return getError(HttpServletResponse.SC_NOT_FOUND, "Este usuario no esta registrado", response);
		}
		/*if (vIdUsuario.equals(vIdUsuarioLogeado)) {
			return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No puedes borrar tu propio usuario", response);
		}*/
		
		JSONObject result = new JSONObject();
		
		String sql = "update tbl_usuario set nombres = '" + vNombres + "' , nick = '" + vNick + "' where id_usuario= '" + vIdUsuario + "'";
		boolean resultado = modelo.actualizarTodo(sql);
		result.put("resultado_modificar_usuario", resultado); 
				
		return "" + result;
	

}
	
	
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para crear un nuevo rol", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "rol", description = "Nombres del rol", required = true, type = STRING),
			 
			})
	public String servicio_save_rol(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vRol = getDatoEntrada("rol", request);

		Rol validadorRol = new Rol(modelo);
		validadorRol = (Rol) validadorRol.buscarElemento(" where rol like '%" + vRol + "%'");
		if (validadorRol != null) {
			return getError(HttpServletResponse.SC_FOUND, "Este rol ya esta registrado", response);
		}

		Rol rol = new Rol(modelo);
		rol.setRol(vRol);

		rol.guardar();

		JSONObject result = rol.toJson();

		return "" + result;

	}
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para borrar un rol", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "idRol", description = "id del rol", required = true, type = STRING),
			 
			})
	public String servicio_remove_rol(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vIdRol = getDatoEntrada("idRol", request);

		Rol validadorRol = new Rol(modelo);
		validadorRol = (Rol) validadorRol.buscarElemento(" where id_rol = '" + vIdRol + "'");
		if (validadorRol == null) {
			return getError(HttpServletResponse.SC_NOT_FOUND, "Este rol no existe", response);
		}

		JSONObject result = new JSONObject();
		
		String sql = "delete from tbl_rol where id_rol= '" + vIdRol + "'";
		boolean resultado = modelo.actualizarTodo(sql);
		result.put("resultado_borrado_contrasena", resultado); 
				
		return "" + result;

	}
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para crear una ficha", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "ficha", description = "Numero de la ficha", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "programa", description = "Nombres del programa", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "fechaInicio", description = "Fecha de inicio de la ficha", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "fechaFin", description = "Fecha de fin de la ficha", required = true, type = STRING),
			 
			})
	public String servicio_save_ficha(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vFicha = getDatoEntrada("ficha", request);
		String vPrograma = getDatoEntrada("programa", request);
		String vFechaIni = getDatoEntrada("fechaInicio", request);
		String vFechaFin = getDatoEntrada("fechaFin", request);
		

		Ficha validadorFicha = new Ficha(modelo);
		validadorFicha = (Ficha) validadorFicha.buscarElemento(" where ficha = '" + vFicha + "'");
		if (validadorFicha != null) {
			return getError(HttpServletResponse.SC_FOUND, "Esta ficha ya esta registrada", response);
		}

		Ficha ficha = new Ficha(modelo);
		ficha.setFicha(vFicha);
		ficha.setPrograma(vPrograma);
		ficha.setFechaInicio(vFechaIni);
		ficha.setFechaFin(vFechaFin);

		ficha.guardar();

		JSONObject result = ficha.toJson();

		return "" + result;

	}
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para borrar un rol", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "idFicha", description = "id del rol", required = true, type = STRING),
			 
			})
	public String servicio_remove_ficha(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vIdFicha = getDatoEntrada("idFicha", request);

		Ficha validadorFicha = new Ficha(modelo);
		validadorFicha = (Ficha) validadorFicha.buscarElemento(" where id_ficha = '" + vIdFicha + "'");
		if (validadorFicha == null) {
			return getError(HttpServletResponse.SC_NOT_FOUND, "Esta ficha no existe", response);
		}

		JSONObject result = new JSONObject();
		
		String sql = "delete from tbl_ficha where id_rol= '" + vIdFicha + "'";
		boolean resultado = modelo.actualizarTodo(sql);
		result.put("resultado_borrado_contrasena", resultado); 
				
		return "" + result;

	}
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para crear una nueva tematica", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "nombre_tematica", description = "nombre de la tematica", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idFicha", description = "id de la ficha a la que pertenece la tematica", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idInstructor", description = "id del instructor al que pertenece la tematica", required = true, type = STRING), 
			})
	public String servicio_save_tematica(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vNomTematica = getDatoEntrada("nombre_tematica", request);
		String vIdFicha = getDatoEntrada("idFicha", request);
		String vIdInstructor = getDatoEntrada("idInstructor", request);
		
		Tematica validadorInstructor = new Tematica(modelo);
		validadorInstructor = (Tematica) validadorInstructor.buscarElemento(" where id_instructor_fk = '" + vIdInstructor + "' and nombre_tematica like '%" + vNomTematica + "%' and id_ficha_fk = '" + vIdFicha + "'");
		if (validadorInstructor != null) {
			return getError(HttpServletResponse.SC_FOUND, "Esta tematica ya fue registrada al instructor", response);
		}
		
		Tematica validadorFicha = new Tematica(modelo);
		validadorFicha = (Tematica) validadorFicha.buscarElemento(" where id_ficha_fk = '" + vIdFicha + "' and nombre_tematica like '%" + vNomTematica + "%'");
		if (validadorFicha != null) {
			return getError(HttpServletResponse.SC_FOUND, "Esta tematica ya fue asignada a la ficha", response);
		}

		Tematica tematica = new Tematica(modelo);
		tematica.setNombreTematica(vNomTematica);
		tematica.setIdInstructorFk(vIdInstructor);
		tematica.setIdFichaFk(vIdFicha);

		tematica.guardar();

		JSONObject result = tematica.toJson();

		return "" + result;

	}
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para borrar una tematica", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "idTematica", description = "id de la tematica a borrar", required = true, type = STRING),
			 
			})
	public String servicio_remove_tematica(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vIdTematica = getDatoEntrada("idTematica", request);

		Tematica validadorTematica = new Tematica(modelo);
		validadorTematica = (Tematica) validadorTematica.buscarElemento(" where id_tematica = '" + vIdTematica + "'");
		if (validadorTematica == null) {
			return getError(HttpServletResponse.SC_NOT_FOUND, "Esta tematica no existe", response);
		}

		JSONObject result = new JSONObject();
		
		String sql = "delete from tbl_tematica where id_tematica= '" + vIdTematica + "'";
		boolean resultado = modelo.actualizarTodo(sql);
		result.put("resultado_borrado_contrasena", resultado); 
				
		return "" + result;

	}
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para actualizar un usuario", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "nombreTematica", description = "Nombres de la tematica", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idInstructorFk", description = "id del instructor", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idFichaFk", description = "id de la ficha", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idTematica", description = "id de la tematica", required = true, type = STRING)
			})
	public String servicio_update_tematica(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vNombreTema = getDatoEntrada("nombreTematica", request);
		String vIdInstructorFk = getDatoEntrada("idInstructorFk", request);
		String vIdFichaFk = getDatoEntrada("idFichaFk", request);
		String vIdTematica = getDatoEntrada("idTematica", request);

		Tematica validador = new Tematica(modelo);
		validador = (Tematica) validador.buscarElemento(" where id_tematica = '" + vIdTematica + "' ");
		if (validador == null) {
			return getError(HttpServletResponse.SC_NOT_FOUND, "La tematica no esta registrada", response);
		}

		Tematica tem = validador;
		tem.setNombreTematica(vNombreTema);
		tem.setIdInstructorFk(vIdInstructorFk);
		tem.setIdFichaFk(vIdFichaFk);
		
		
		tem.actualizar(" where id_tematica = '" + vIdTematica + "' ");

		JSONObject result = tem.toJson();

		return "" + result;

	}
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para asignar una ficha a un usuario", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "idUsuario", description = "id del usuario", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idFicha", description = "id de la ficha", required = true, type = STRING),
			})
	public String servicio_save_fichas_usuarios(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vIdUsuario = getDatoEntrada("idUsuario", request);
		String vIdFicha = getDatoEntrada("idFicha", request);

		UsuarioFicha validadorUsFicha = new UsuarioFicha(modelo);
		validadorUsFicha = (UsuarioFicha) validadorUsFicha.buscarElemento(" where id_usuario_fk = '" + vIdUsuario + "' and id_ficha_fk = '" + vIdFicha + "'");
		if (validadorUsFicha != null) {
			return getError(HttpServletResponse.SC_FOUND, "Este usuario ya esta registrado en la ficha seleccionada", response);
		}

		UsuarioFicha usFicha = new UsuarioFicha(modelo);
		usFicha.setIdUsuario(vIdUsuario);
		usFicha.setIdFicha(vIdFicha);

		usFicha.guardar();

		JSONObject result = usFicha.toJson();

		return "" + result;

	}
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para asignar una ficha a un usuario", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "idUsuario", description = "id del usuario", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idFicha", description = "id de la ficha", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idTematica", description = "id de la tematica", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idFicha", description = "id de la ficha", required = true, type = STRING),
			})
	public String servicio_save_evidencias(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vIdUsuario = getDatoEntrada("idUsuario", request);
		String vIdFicha = getDatoEntrada("idFicha", request);

		Usuario validadorUsFicha = new Usuario(modelo);
		validadorUsFicha = (Usuario) validadorUsFicha.buscarElemento(" where id_usuario_fk = '" + vIdUsuario + "' and id_ficha_fk = '" + vIdFicha + "'");
		if (validadorUsFicha != null) {
			return getError(HttpServletResponse.SC_NOT_FOUND, "Este usuario no exite", response);
		}
		
		return "" + result;

		

	}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar a los aprendices ",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "id_ficha", description = "id de la ficha seleccionada ", required = true, type = STRING)
		})
		public String servicio_get_aprendices(HttpServletRequest request, HttpServletResponse response) throws Exception {
			
			String vIdFicha = getDatoEntrada("id_ficha", request);
			
			String sql = "SELECT * FROM tbl_usuario,tbl_usuario_ficha,tbl_rol,tbl_ficha \r\n"
					+ "WHERE tbl_usuario.id_usuario = tbl_usuario_ficha.id_usuario_fk \r\n"
					+ "and tbl_ficha.id_ficha = tbl_usuario_ficha.id_ficha_fk\r\n"
					+ "AND tbl_usuario.id_rol_fk = tbl_rol.id_rol \r\n"
					+ "AND tbl_rol.id_rol = 'ROL-900ed58f'\r\n"
					+ "and tbl_ficha.id_ficha = '" + vIdFicha + "'";
			return "" + modelo.executeQuery(sql);

		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar las fichas",
			parametros = {
					
		})
		public String servicio_get_fichas(HttpServletRequest request, HttpServletResponse response) throws Exception {
			
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLog = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
			
			String sql = "SELECT * FROM tbl_ficha,tbl_usuario, tbl_usuario_ficha\r\n"
					+ "where tbl_usuario.id_usuario = tbl_usuario_ficha.id_usuario_fk\r\n"
					+ "and tbl_ficha.id_ficha = tbl_usuario_ficha.id_ficha_fk\r\n"
					+ "and tbl_usuario.id_usuario = '" + vIdUsuarioLog + "'";
			return "" + modelo.executeQuery(sql);

		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar las tematicas instructor",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "id_ficha", description = "id de la ficha seleccionada ", required = true, type = STRING)
					
		})
		public String servicio_get_tematicas_ins(HttpServletRequest request, HttpServletResponse response) throws Exception {
			
		String vIdFicha = getDatoEntrada("id_ficha", request);
		
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLog = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
		
		String sql = "SELECT * FROM tbl_tematica where id_ficha_fk = '" + vIdFicha + "' and id_instructor_fk = '" + vIdUsuarioLog + "'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar las tematicas aprendiz ",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "id_ficha", description = "id de la ficha seleccionada ", required = true, type = STRING)
					
		})
		public String servicio_get_tematicas_apr(HttpServletRequest request, HttpServletResponse response) throws Exception {
			
		String vIdFicha = getDatoEntrada("id_ficha", request);
				
		String sql = "SELECT * FROM tbl_tematica where id_ficha_fk = '" + vIdFicha + "'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(metodo = POST, 
			acceso = PRIVADO, 
			descripcion = "Servicio que sirve para registrar la asistencia", 
			parametros = {
			@PropiedadParametro(in = BODY, name = "fechaAsiste", description = "Fecha de la asistencia", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "tomaAsiste", description = "Toma de asistencia", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idTematicaFk", description = "id de la tematica", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idFichaFk", description = "id de la ficha donde se toma la asistencia", required = true, type = STRING),
			@PropiedadParametro(in = BODY, name = "idAprendizFk", description = "id del Aprendiz", required = true, type = STRING),
			 
			})
	public String servicio_save_asistencia(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String vFechaAs = getDatoEntrada("fechaAsiste", request);
		String vTomaAs = getDatoEntrada("tomaAsiste", request);
		String vIdTematicaFk = getDatoEntrada("idTematicaFk", request);
		String vIdFichaFk = getDatoEntrada("idFichaFk", request);
		String vIdAprendizFk = getDatoEntrada("idAprendizFk", request);
		
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLog = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");

		Asistencia validadorAs = new Asistencia(modelo);
		validadorAs = (Asistencia) validadorAs.buscarElemento(" where id_aprendiz_fk = '" + vIdAprendizFk + "' and id_ficha_fk = '" + vIdFichaFk + "' and id_instructor_fk = '" + vIdUsuarioLog + "' and id_tematica_fk = '" + vIdTematicaFk + "' and fecha_asistencia = '" + vFechaAs + "'");
		if (validadorAs != null) {
			Asistencia asisUp = new Asistencia(modelo);
			asisUp.setFechaAsistencia(vFechaAs);
			asisUp.setTomaAsistencia(vTomaAs);
			asisUp.setIdTematicaFk(vIdTematicaFk);
			asisUp.setIdInstructorFk(vIdUsuarioLog);
			asisUp.setIdFichaFk(vIdFichaFk);
			asisUp.setIdAprendizFk(vIdAprendizFk);
			
			asisUp.actualizar(" where id_aprendiz_fk = '" + vIdAprendizFk + "' and id_ficha_fk = '" + vIdFichaFk + "' and id_instructor_fk = '" + vIdUsuarioLog + "' and id_tematica_fk = '" + vIdTematicaFk + "' and fecha_asistencia = '" + vFechaAs + "'");
			
			JSONObject result = asisUp.toJson();

			return "" + result;
		}
		

		Asistencia asis = new Asistencia(modelo);
		asis.setFechaAsistencia(vFechaAs);
		asis.setTomaAsistencia(vTomaAs);
		asis.setIdTematicaFk(vIdTematicaFk);
		asis.setIdInstructorFk(vIdUsuarioLog);
		asis.setIdFichaFk(vIdFichaFk);
		asis.setIdAprendizFk(vIdAprendizFk);
		

		asis.guardar();

		JSONObject result = asis.toJson();

		return "" + result;

	}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar las asistencias",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "id_ficha", description = "id de la ficha seleccionada ", required = true, type = STRING),
					@PropiedadParametro(in = QUERY, name = "id_tematica", description = "id de la de la tematica seleccionada ", required = true, type = STRING),
					@PropiedadParametro(in = QUERY, name = "fechaAsiste", description = "Fecha de la consulta", required = true, type = STRING),
					
		})
		public String servicio_get_asistencia_ins(HttpServletRequest request, HttpServletResponse response) throws Exception {
			
		String vIdFicha = getDatoEntrada("id_ficha", request);
		String vIdTematica = getDatoEntrada("id_tematica", request);
		String vFechaAsiste = getDatoEntrada("fechaAsiste", request);
		
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLog = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
		
		String sql = "SELECT tbl_usuario.id_usuario,tbl_usuario.tipo_doc, tbl_usuario.documento, tbl_usuario.nombres, tbl_usuario.apellidos, tbl_usuario.nick, tbl_usuario.estado, tbl_asistencia.toma_asistencia FROM tbl_asistencia,tbl_usuario,tbl_usuario_ficha,tbl_rol,tbl_ficha\r\n"
				+ "WHERE tbl_usuario.id_usuario = tbl_usuario_ficha.id_usuario_fk \r\n"
				+ "and tbl_ficha.id_ficha = tbl_usuario_ficha.id_ficha_fk\r\n"
				+ "AND tbl_usuario.id_rol_fk = tbl_rol.id_rol \r\n"
				+ "AND tbl_rol.id_rol = 'ROL-900ed58f'\r\n"
				+ "AND tbl_asistencia.fecha_asistencia = '" + vFechaAsiste + "'\r\n"
				+ "AND tbl_asistencia.id_instructor_fk = '" + vIdUsuarioLog + "'\r\n"
				+ "and tbl_asistencia.id_tematica_fk = '" + vIdTematica + "'\r\n"
				+ "and tbl_usuario.id_usuario = tbl_asistencia.id_aprendiz_fk\r\n"
				+ "and tbl_ficha.id_ficha = '"+ vIdFicha + "' group by tbl_usuario.id_usuario;";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar las asistencias",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "id_ficha", description = "id de la ficha seleccionada ", required = true, type = STRING),
					@PropiedadParametro(in = QUERY, name = "fechaAsiste", description = "Fecha de la consulta", required = true, type = STRING),
					
		})
		public String servicio_get_asistencia_apr(HttpServletRequest request, HttpServletResponse response) throws Exception {
			
		String vIdFicha = getDatoEntrada("id_ficha", request);
		String vFechaAsiste = getDatoEntrada("fechaAsiste", request);
		
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLog = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
		
		String sql = "SELECT * FROM tbl_asistencia,tbl_tematica,tbl_usuario\r\n"
				+ "WHERE tbl_tematica.id_instructor_fk = tbl_usuario.id_usuario\r\n"
				+ "AND tbl_asistencia.id_tematica_fk = tbl_tematica.id_tematica\r\n"
				+ "AND tbl_tematica.id_ficha_fk = '" + vIdFicha + "'\r\n"
				+ "AND tbl_asistencia.id_aprendiz_fk = '" + vIdUsuarioLog + "'\r\n"
				+ "AND tbl_asistencia.fecha_asistencia = '" + vFechaAsiste + "'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = POST, 
			acceso = PUBLICO, 
			descripcion = "Servicio para enviar un correo electronico",
			parametros = {
					@PropiedadParametro(in = BODY, name = "nick", description = "correo del aprendiz a notificar", required = true, type = STRING),
					@PropiedadParametro(in = BODY, name = "razon", description = "razón de la notificación", required = true, type = STRING),
					@PropiedadParametro(in = BODY, name = "asunto", description = "asunto del correo", required = false, type = STRING),
					@PropiedadParametro(in = BODY, name = "idFicha", description = "id de la ficha", required = true, type = STRING),
		})
		public String servicio_send_correo(HttpServletRequest request, HttpServletResponse response) throws Exception {
			
		String remitente = "notificadorasistencia2@gmail.com";
		String clave = "notificador2021";
		String vNick = getDatoEntrada("nick", request);
		String vRazon = getDatoEntrada("razon", request);
		String vAsunto = getDatoEntrada("razon", request);
		String vFicha = getDatoEntrada("idFicha", request);
		
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLog = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
		
		JSONObject resultado = new JSONObject();
		
		Properties props = new Properties();
		props.put("mail.stmp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.user", remitente);
		props.put("mail.smtp.password", clave);
		
		Session session = Session.getDefaultInstance(props);
		
		MimeMessage mensaje = new MimeMessage(session);
		
		try {
			mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(vNick));
			
			if(vRazon.equals("Notificacion inasistencias")) {
				String nombreAprendiz = "";
				String programa = "";
				String numFicha = "";
				String tiempo = "" + CoviFecha.ahora();
				
				Usuario validarNombre = new Usuario(modelo);
				validarNombre = (Usuario)validarNombre.buscarElemento("where nick = '" + vNick + "'");
				if(validarNombre == null) {
					return getError(HttpServletResponse.SC_NOT_FOUND, "Este usuario no existe", response);
				}
				nombreAprendiz = validarNombre.getApellidos() + " " + validarNombre.getNombres();
				
				Ficha validarFicha = new Ficha(modelo);
				validarFicha = (Ficha)validarFicha.buscarElemento("where id_ficha = '" + vFicha + "'");
				if(validarFicha == null) {
					return getError(HttpServletResponse.SC_NOT_FOUND, "Esta ficha no existe", response);
				}
				programa = validarFicha.getPrograma();
				numFicha = validarFicha.getFicha();
				
				mensaje.setSubject("Notificacion inasistencias, Ficha: " + numFicha + " - Control Asistencia");
				
				BodyPart parteTexto = new MimeBodyPart();
				parteTexto.setContent("<!DOCTYPE html>\r\n"
						+ "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\r\n"
						+ "<head>\r\n"
						+ "	<meta charset=\"UTF-8\">\r\n"
						+ "	<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\r\n"
						+ "	<meta name=\"x-apple-disable-message-reformatting\">\r\n"
						+ "	<title></title>\r\n"
						+ "	<style>\r\n"
						+ "		table, td, div, h1, p {font-family: Arial, sans-serif;}\r\n"
						+ "	</style>\r\n"
						+ "</head>\r\n"
						+ "<body style=\"margin:0;padding:0;\">\r\n"
						+ "	<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;background:#ffffff;\">\r\n"
						+ "		<tr>\r\n"
						+ "			<td align=\"center\" style=\"padding:0;\">\r\n"
						+ "				<table role=\"presentation\" style=\"width:690px;border-collapse:collapse;border:1px solid #cccccc;border-spacing:0;text-align:left;\">\r\n"
						+ "					<tr>\r\n"
						+ "						<td align=\"center\" style=\"padding:40px 0 30px 0;\">\r\n"
						+ "							<img src=\"https://raw.githubusercontent.com/JuanRx/imagenes.github.io/main/logoP.png\" alt=\"\" width=\"300\" style=\"height:auto;display:block;\" />\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "					<tr>\r\n"
						+ "						<td style=\"padding:36px 30px 42px 30px;\">\r\n"
						+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;\">\r\n"
						+ "								<tr>\r\n"
						+ "									<td style=\"padding:0 0 36px 0;color:#153643;\">\r\n"
						+ "										<h1 style=\"font-size:24px;margin:0 0 20px 0;font-family:Arial,sans-serif;\">Notificación inasistencia al proceso de formación</h1>\r\n"
						+ "										<p style=\"margin:0 0 12px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\">Cordial saludo " + nombreAprendiz + "<br> "
						+ "											<b>Aprendiz del " + programa + "</b> <br><br>"
						+ "											Teniendo en cuenta el Reglamento de Aprendiz en el capitulo VII, articulo 22. 'Proceso de formación, incumplimiento y deserción'. <br>"
						+ "											Se le notifica la inasistencia al proceso de formacion del dia <b>" + CoviFecha.ahora() + "</b> ,completando. (1) día sin presentarse"
						+ "											al centro de formación. <br>"
						+ "											Por tal motivo es necesario que presente el soporte válido. dentro de los dos (2) días hábiles siguientes a partir de la fecha que "
						+ "											su ausencia o incumplimiento de las actividades. <br>"
						+ "											<h3>Resumen de inasistencias<br> <p>" + tiempo + "<br> " + programa + "</p><br></h3> "
						+ "											<b>Atentamente, <br> Equipo de instructores etapa lectiva</b>"
						+ "										</p>\r\n"
						+ "									</td>\r\n"
						+ "								</tr>\r\n"
						+ "							</table>\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "					<tr>\r\n"
						+ "						<td style=\"padding:30px;background:#fc6924;\">\r\n"
						+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;font-size:9px;font-family:Arial,sans-serif;\">\r\n"
						+ "								<tr>\r\n"
						+ "									<td style=\"padding:0;width:50%;\" align=\"left\">\r\n"
						+ "										<p style=\"margin:0;font-size:14px;line-height:16px;font-family:Arial,sans-serif;color:#ffffff;\">\r\n"
						+ "											&reg; Presentel 2021<br/><a href=\"http://www.example.com\" style=\"color:#ffffff;text-decoration:underline;\">Copyright</a>\r\n"
						+ "										</p>\r\n"
						+ "									</td>\r\n"
						+ "									<td style=\"padding:0;width:50%;\" align=\"right\">\r\n"
						+ "										<table role=\"presentation\" style=\"border-collapse:collapse;border:0;border-spacing:0;\">\r\n"
						+ "											<tr>\r\n"
						+ "												<td style=\"padding:0 0 0 10px;width:38px;\">\r\n"
						+ "													<a href=\"http://www.twitter.com/\" style=\"color:#ffffff;\"><img src=\"images/tw.png\" alt=\"Twitter\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\r\n"
						+ "												</td>\r\n"
						+ "												<td style=\"padding:0 0 0 10px;width:38px;\">\r\n"
						+ "													<a href=\"http://www.facebook.com/\" style=\"color:#ffffff;\"><img src=\"images/fb.png\" alt=\"Facebook\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\r\n"
						+ "												</td>\r\n"
						+ "											</tr>\r\n"
						+ "										</table>\r\n"
						+ "									</td>\r\n"
						+ "								</tr>\r\n"
						+ "							</table>\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "				</table>\r\n"
						+ "			</td>\r\n"
						+ "		</tr>\r\n"
						+ "	</table>\r\n"
						+ "</body>\r\n"
						+ "</html>","text/html");
				
				MimeMultipart totalMensaje = new MimeMultipart();
				totalMensaje.addBodyPart(parteTexto);
				
				mensaje.setContent(totalMensaje);
				
				Notificaciones notif = new Notificaciones(modelo);
				notif.setFechaNotifica(tiempo);
				notif.setIdUsuarioNoti(vIdUsuarioLog);
				notif.setIdUsuarioNotif("" + validarNombre.getIdUsuario());
				notif.setMotivo("Notificacion Inasistencias");
				
				notif.guardar();
			}
			
			if(vRazon.equals("Recuperar contrasena")) {
				String nombre = "";
				String contrasenaGen =  generarContrasena(10);
				
				Usuario validarNombre = new Usuario(modelo);
				validarNombre = (Usuario)validarNombre.buscarElemento("where nick = '" + vNick + "'");
				if(validarNombre == null) {
					return getError(HttpServletResponse.SC_NOT_FOUND, "Este usuario no existe", response);
				}
				nombre = validarNombre.getNombres();
				
				validarNombre.setContrasena(contrasenaGen);
				validarNombre.generarContrasena(contrasenaGen);
				validarNombre.actualizar("where nick = '" + vNick + "'");
				
				
				
				
				mensaje.setSubject("Recuperación de tu contraseña - Control Asistencia");
				
				BodyPart parteTexto = new MimeBodyPart();
				parteTexto.setContent("<!DOCTYPE html>\r\n"
						+ "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\r\n"
						+ "<head>\r\n"
						+ "	<meta charset=\"UTF-8\">\r\n"
						+ "	<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\r\n"
						+ "	<meta name=\"x-apple-disable-message-reformatting\">\r\n"
						+ "	<title></title>\r\n"
						+ "	<style>\r\n"
						+ "		table, td, div, h1, p {font-family: Arial, sans-serif;}\r\n"
						+ "	</style>\r\n"
						+ "</head>\r\n"
						+ "<body style=\"margin:0;padding:0;\">\r\n"
						+ "	<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;background:#ffffff;\">\r\n"
						+ "		<tr>\r\n"
						+ "			<td align=\"center\" style=\"padding:0;\">\r\n"
						+ "				<table role=\"presentation\" style=\"width:602px;border-collapse:collapse;border:1px solid #cccccc;border-spacing:0;text-align:left;\">\r\n"
						+ "					<tr>\r\n"
						+ "						<td align=\"center\" style=\"padding:40px 0 30px 0;\">\r\n"
						+ "							<img src=\"https://raw.githubusercontent.com/JuanRx/imagenes.github.io/main/logoP.png\" alt=\"\" width=\"300\" style=\"height:auto;display:block;\" />\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "					<tr>\r\n"
						+ "						<td style=\"padding:36px 30px 42px 30px;\">\r\n"
						+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;\">\r\n"
						+ "								<tr>\r\n"
						+ "									<td style=\"padding:0 0 36px 0;color:#153643;\">\r\n"
						+ "										<h1 style=\"font-size:24px;margin:0 0 20px 0;font-family:Arial,sans-serif;\">Hola, " + nombre + "</h1>\r\n"
						+ "										<p style=\"margin:0 0 12px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\">Recibimos una solicitud para restablecer tu contraseña de Presentel<br> Por favor ingrese con la siguiente contraseña</p>\r\n"
						+ "										<p style=\"margin:0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\"><input type=\"text\" readonly value="+ contrasenaGen + " style=\"border-color: #fc6924;border-radius: 5px;border_style = solid;\"></p>\r\n"
						+ "									</td>\r\n"
						+ "								</tr>\r\n"
						+ "							</table>\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "					<tr>\r\n"
						+ "						<td style=\"padding:30px;background:#fc6924;\">\r\n"
						+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;font-size:9px;font-family:Arial,sans-serif;\">\r\n"
						+ "								<tr>\r\n"
						+ "									<td style=\"padding:0;width:50%;\" align=\"left\">\r\n"
						+ "										<p style=\"margin:0;font-size:14px;line-height:16px;font-family:Arial,sans-serif;color:#ffffff;\">\r\n"
						+ "											&reg; Presentel 2021<br/><a href=\"http://www.example.com\" style=\"color:#ffffff;text-decoration:underline;\">Copyright</a>\r\n"
						+ "										</p>\r\n"
						+ "									</td>\r\n"
						+ "									<td style=\"padding:0;width:50%;\" align=\"right\">\r\n"
						+ "										<table role=\"presentation\" style=\"border-collapse:collapse;border:0;border-spacing:0;\">\r\n"
						+ "											<tr>\r\n"
						+ "												<td style=\"padding:0 0 0 10px;width:38px;\">\r\n"
						+ "													<a href=\"http://www.twitter.com/\" style=\"color:#ffffff;\"><img src=\"images/tw.png\" alt=\"Twitter\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\r\n"
						+ "												</td>\r\n"
						+ "												<td style=\"padding:0 0 0 10px;width:38px;\">\r\n"
						+ "													<a href=\"http://www.facebook.com/\" style=\"color:#ffffff;\"><img src=\"images/fb.png\" alt=\"Facebook\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\r\n"
						+ "												</td>\r\n"
						+ "											</tr>\r\n"
						+ "										</table>\r\n"
						+ "									</td>\r\n"
						+ "								</tr>\r\n"
						+ "							</table>\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "				</table>\r\n"
						+ "			</td>\r\n"
						+ "		</tr>\r\n"
						+ "	</table>\r\n"
						+ "</body>\r\n"
						+ "</html>","text/html");
				
				MimeMultipart totalMensaje = new MimeMultipart();
				totalMensaje.addBodyPart(parteTexto);
				
				mensaje.setContent(totalMensaje);
			}
			
			if(vRazon.equals("Usuario Nuevo")) {
				String nombre = "";
				String contrasenaGen =  generarContrasena(10);
				
				Usuario validarNombre = new Usuario(modelo);
				validarNombre = (Usuario)validarNombre.buscarElemento("where nick = '" + vNick + "'");
				if(validarNombre == null) {
					return getError(HttpServletResponse.SC_NOT_FOUND, "Este usuario no existe", response);
				}
				nombre = validarNombre.getNombres();
				
				validarNombre.setContrasena(contrasenaGen);
				validarNombre.generarContrasena(contrasenaGen);
				validarNombre.actualizar("where nick = '" + vNick + "'");
				
				
				
				
				mensaje.setSubject("Creacion de tu usuario - Control Asistencia");
				
				BodyPart parteTexto = new MimeBodyPart();
				parteTexto.setContent("<!DOCTYPE html>\r\n"
						+ "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\r\n"
						+ "<head>\r\n"
						+ "	<meta charset=\"UTF-8\">\r\n"
						+ "	<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\r\n"
						+ "	<meta name=\"x-apple-disable-message-reformatting\">\r\n"
						+ "	<title></title>\r\n"
						+ "	<style>\r\n"
						+ "		table, td, div, h1, p {font-family: Arial, sans-serif;}\r\n"
						+ "	</style>\r\n"
						+ "</head>\r\n"
						+ "<body style=\"margin:0;padding:0;\">\r\n"
						+ "	<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;background:#ffffff;\">\r\n"
						+ "		<tr>\r\n"
						+ "			<td align=\"center\" style=\"padding:0;\">\r\n"
						+ "				<table role=\"presentation\" style=\"width:602px;border-collapse:collapse;border:1px solid #cccccc;border-spacing:0;text-align:left;\">\r\n"
						+ "					<tr>\r\n"
						+ "						<td align=\"center\" style=\"padding:40px 0 30px 0;\">\r\n"
						+ "							<img src=\"https://raw.githubusercontent.com/JuanRx/imagenes.github.io/main/logoP.png\" alt=\"\" width=\"300\" style=\"height:auto;display:block;\" />\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "					<tr>\r\n"
						+ "						<td style=\"padding:36px 30px 42px 30px;\">\r\n"
						+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;\">\r\n"
						+ "								<tr>\r\n"
						+ "									<td style=\"padding:0 0 36px 0;color:#153643;\">\r\n"
						+ "										<h1 style=\"font-size:24px;margin:0 0 20px 0;font-family:Arial,sans-serif;\">Hola " + nombre + " , Bienvenid@ a Presentel</h1>\r\n"
						+ "										<p style=\"margin:0 0 12px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\">Recibimos una solicitud para crear tu usuario en el sistema,<br> Por favor ingrese con la siguiente contraseña</p>\r\n"
						+ "										<p style=\"margin:0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\"><input type=\"text\" readonly value="+ contrasenaGen + " style=\"border-color: #fc6924;border-radius: 5px;\"></p>\r\n"
						+ "									</td>\r\n"
						+ "								</tr>\r\n"
						+ "							</table>\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "					<tr>\r\n"
						+ "						<td style=\"padding:30px;background:#fc6924;\">\r\n"
						+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;font-size:9px;font-family:Arial,sans-serif;\">\r\n"
						+ "								<tr>\r\n"
						+ "									<td style=\"padding:0;width:50%;\" align=\"left\">\r\n"
						+ "										<p style=\"margin:0;font-size:14px;line-height:16px;font-family:Arial,sans-serif;color:#ffffff;\">\r\n"
						+ "											&reg; Presentel 2021<br/><a href=\"http://www.example.com\" style=\"color:#ffffff;text-decoration:underline;\">Copyright</a>\r\n"
						+ "										</p>\r\n"
						+ "									</td>\r\n"
						+ "									<td style=\"padding:0;width:50%;\" align=\"right\">\r\n"
						+ "										<table role=\"presentation\" style=\"border-collapse:collapse;border:0;border-spacing:0;\">\r\n"
						+ "											<tr>\r\n"
						+ "												<td style=\"padding:0 0 0 10px;width:38px;\">\r\n"
						+ "													<a href=\"http://www.twitter.com/\" style=\"color:#ffffff;\"><img src=\"images/tw.png\" alt=\"Twitter\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\r\n"
						+ "												</td>\r\n"
						+ "												<td style=\"padding:0 0 0 10px;width:38px;\">\r\n"
						+ "													<a href=\"http://www.facebook.com/\" style=\"color:#ffffff;\"><img src=\"images/fb.png\" alt=\"Facebook\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\r\n"
						+ "												</td>\r\n"
						+ "											</tr>\r\n"
						+ "										</table>\r\n"
						+ "									</td>\r\n"
						+ "								</tr>\r\n"
						+ "							</table>\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "				</table>\r\n"
						+ "			</td>\r\n"
						+ "		</tr>\r\n"
						+ "	</table>\r\n"
						+ "</body>\r\n"
						+ "</html>","text/html");
				
				MimeMultipart totalMensaje = new MimeMultipart();
				totalMensaje.addBodyPart(parteTexto);
				
				mensaje.setContent(totalMensaje);
			}
			
			if(vRazon.equals("Envio Excusa")) {
				
				String nombreApr = "";
				String nombreIns = "";
				
				
				Usuario validarNombreIns = new Usuario(modelo);
				validarNombreIns = (Usuario)validarNombreIns.buscarElemento("where id_usuario = '" + vNick + "'");
				if(validarNombreIns == null) {
					return getError(HttpServletResponse.SC_NOT_FOUND, "Este usuario no existe", response);
				}
				nombreIns = validarNombreIns.getNombres();
				
				Usuario validarNombreApr = new Usuario(modelo);
				validarNombreApr = (Usuario)validarNombreApr.buscarElemento("where id_usuario = '" + vNick + "'");
				if(validarNombreApr == null) {
					return getError(HttpServletResponse.SC_NOT_FOUND, "Este usuario no existe", response);
				}
				nombreIns = validarNombreApr.getNombres() + " " + validarNombreApr.getApellidos();
				
				
				
				mensaje.setSubject("Envio de excusa - Control Asistencia");
				
				BodyPart parteTexto = new MimeBodyPart();
				parteTexto.setContent("<!DOCTYPE html>\r\n"
						+ "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\r\n"
						+ "<head>\r\n"
						+ "	<meta charset=\"UTF-8\">\r\n"
						+ "	<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\r\n"
						+ "	<meta name=\"x-apple-disable-message-reformatting\">\r\n"
						+ "	<title></title>\r\n"
						+ "	<style>\r\n"
						+ "		table, td, div, h1, p {font-family: Arial, sans-serif;}\r\n"
						+ "	</style>\r\n"
						+ "</head>\r\n"
						+ "<body style=\"margin:0;padding:0;\">\r\n"
						+ "	<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;background:#ffffff;\">\r\n"
						+ "		<tr>\r\n"
						+ "			<td align=\"center\" style=\"padding:0;\">\r\n"
						+ "				<table role=\"presentation\" style=\"width:602px;border-collapse:collapse;border:1px solid #cccccc;border-spacing:0;text-align:left;\">\r\n"
						+ "					<tr>\r\n"
						+ "						<td align=\"center\" style=\"padding:40px 0 30px 0;\">\r\n"
						+ "							<img src=\"https://raw.githubusercontent.com/JuanRx/imagenes.github.io/main/logoP.png\" alt=\"\" width=\"300\" style=\"height:auto;display:block;\" />\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "					<tr>\r\n"
						+ "						<td style=\"padding:36px 30px 42px 30px;\">\r\n"
						+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;\">\r\n"
						+ "								<tr>\r\n"
						+ "									<td style=\"padding:0 0 36px 0;color:#153643;\">\r\n"
						+ "										<h1 style=\"font-size:24px;margin:0 0 20px 0;font-family:Arial,sans-serif;\">Hola Instructor " + nombreIns + " , </h1>\r\n"
						+ "										<p style=\"margin:0 0 12px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\">El aprendiz: " + nombreApr + ",<br> Envio la excusa en la tematica</p>\r\n"
						+ "										<p style=\"margin:0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\"><img src=" + vAsunto + "></p>\r\n"
						+ "									</td>\r\n"
						+ "								</tr>\r\n"
						+ "							</table>\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "					<tr>\r\n"
						+ "						<td style=\"padding:30px;background:#fc6924;\">\r\n"
						+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;font-size:9px;font-family:Arial,sans-serif;\">\r\n"
						+ "								<tr>\r\n"
						+ "									<td style=\"padding:0;width:50%;\" align=\"left\">\r\n"
						+ "										<p style=\"margin:0;font-size:14px;line-height:16px;font-family:Arial,sans-serif;color:#ffffff;\">\r\n"
						+ "											&reg; Presentel 2021<br/><a href=\"http://www.example.com\" style=\"color:#ffffff;text-decoration:underline;\">Copyright</a>\r\n"
						+ "										</p>\r\n"
						+ "									</td>\r\n"
						+ "									<td style=\"padding:0;width:50%;\" align=\"right\">\r\n"
						+ "										<table role=\"presentation\" style=\"border-collapse:collapse;border:0;border-spacing:0;\">\r\n"
						+ "											<tr>\r\n"
						+ "												<td style=\"padding:0 0 0 10px;width:38px;\">\r\n"
						+ "													<a href=\"http://www.twitter.com/\" style=\"color:#ffffff;\"><img src=\"images/tw.png\" alt=\"Twitter\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\r\n"
						+ "												</td>\r\n"
						+ "												<td style=\"padding:0 0 0 10px;width:38px;\">\r\n"
						+ "													<a href=\"http://www.facebook.com/\" style=\"color:#ffffff;\"><img src=\"images/fb.png\" alt=\"Facebook\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\r\n"
						+ "												</td>\r\n"
						+ "											</tr>\r\n"
						+ "										</table>\r\n"
						+ "									</td>\r\n"
						+ "								</tr>\r\n"
						+ "							</table>\r\n"
						+ "						</td>\r\n"
						+ "					</tr>\r\n"
						+ "				</table>\r\n"
						+ "			</td>\r\n"
						+ "		</tr>\r\n"
						+ "	</table>\r\n"
						+ "</body>\r\n"
						+ "</html>","text/html");
				
				MimeMultipart totalMensaje = new MimeMultipart();
				totalMensaje.addBodyPart(parteTexto);
				
				mensaje.setContent(totalMensaje);
			}
			
			Transport transport = session.getTransport("smtp");
			transport.connect("smtp.gmail.com", remitente, clave);
			transport.sendMessage(mensaje, mensaje.getAllRecipients());
			transport.close();
			
			
			
			resultado.put("resultado", "Se ha notificado correctamente al aprendiz");
			
			return "" + resultado;
			
		} catch (Exception e) {
			resultado.put("resultado", "Hubo un error al notificar al aprendiz");
			return "" + resultado;
		}
		
	}
		
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar un aprendiz en especifico",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "id_usuario", description = "id del usuario seleccionado ", required = true, type = STRING)
					
		})
		public String servicio_get_info_aprendiz(HttpServletRequest request, HttpServletResponse response) throws Exception {
			
		String vIdUsuario = getDatoEntrada("id_usuario", request);
				
		String sql = "SELECT * FROM tbl_usuario where id_usuario = '" + vIdUsuario + "'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para limpiar la tabla temporal de carga masiva",
			parametros = {
					
		})
		public String servicio_delete_tempCM(HttpServletRequest request, HttpServletResponse response) throws Exception {
							
		String sql = "DELETE FROM tbl_tempCM";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar todos los roles",
			parametros = {
					
		})
		public String servicio_get_roles(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String sql = "Select rol, id_rol from tbl_rol";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar usuarios por roles",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "id_rol", description = "id del rol seleccionado ", required = true, type = STRING)
					
		})
		public String servicio_get_usuarios_rol(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String vIdRol = getDatoEntrada("id_rol", request);
		
		String sql = "SELECT tbl_usuario.nombres, tbl_usuario.apellidos, tbl_usuario.tipo_doc,tbl_usuario.documento, tbl_usuario.estado, tbl_usuario.nick FROM tbl_usuario,tbl_usuario_ficha\r\n"
				+ " where id_rol_fk = '" + vIdRol + "' and tbl_usuario.id_usuario is not tbl_usuario_ficha.id_usuario_fk\r\n"
				+ " group by tbl_usuario.id_usuario";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar todas las fichas",
			parametros = {
					
		})
		public String servicio_get_all_fichas(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String sql = "select id_ficha,ficha from tbl_ficha";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar todas los usuarios",
			parametros = {
					
		})
		public String servicio_get_all_usuarios(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLog = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
		
		String sql = "SELECT * FROM tbl_usuario,tbl_rol\r\n"
				+ "WHERE tbl_usuario.id_rol_fk = tbl_rol.id_rol\r\n"
				+ "AND tbl_usuario.id_usuario <> '" + vIdUsuarioLog + "'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar un usuario especifico",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "id_usuario", description = "id del usuario seleccionado ", required = true, type = STRING)	
					
		})
		public String servicio_get_usuario(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String vIdUsuario = getDatoEntrada("id_usuario", request);
		
		String sql = "SELECT * FROM tbl_usuario,tbl_rol\r\n"
				+ "WHERE tbl_usuario.id_rol_fk = tbl_rol.id_rol\r\n"
				+ "AND tbl_usuario.id_usuario = '" + vIdUsuario + "'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar todos los usuarios sin ficha asignada",
			parametros = {
					
		})
		public String servicio_get_usu_sin_ficha(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String sql = "SELECT * FROM tbl_usuario\r\n"
				+ "WHERE tbl_usuario.id_usuario NOT IN (SELECT tbl_usuario_ficha.id_usuario_fk FROM tbl_usuario_ficha)\r\n"
				+ "AND tbl_usuario.id_rol_fk NOT IN ('ROL-58ce1734','ROL-697e0579');";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar todas las tematicas",
			parametros = {
					
		})
		public String servicio_get_all_tematicas(HttpServletRequest request, HttpServletResponse response) throws Exception {		
		
		String sql = "SELECT tbl_tematica.id_tematica,tbl_tematica.nombre_tematica,tbl_usuario.nombres,tbl_usuario.apellidos,tbl_ficha.ficha,tbl_ficha.programa\r\n"
				+ "FROM tbl_tematica,tbl_usuario,tbl_ficha\r\n"
				+ "WHERE tbl_tematica.id_instructor_fk = tbl_usuario.id_usuario\r\n"
				+ "AND tbl_tematica.id_ficha_fk = tbl_ficha.id_ficha";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar todas las tematicas",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "id_tematica", description = "id del usuario seleccionado ", required = true, type = STRING)
					
		})
		public String servicio_get_tematica(HttpServletRequest request, HttpServletResponse response) throws Exception {		
		
		String vIdTematica = getDatoEntrada("id_tematica", request);
		
		String sql = "SELECT tbl_tematica.nombre_tematica,tbl_tematica.id_instructor_fk,tbl_tematica.id_ficha_fk,tbl_usuario.nombres,tbl_usuario.apellidos,tbl_usuario.nick,tbl_ficha.ficha,tbl_ficha.programa\r\n"
				+ "FROM tbl_tematica,tbl_usuario,tbl_ficha\r\n"
				+ "WHERE tbl_tematica.id_instructor_fk = tbl_usuario.id_usuario\r\n"
				+ "AND tbl_tematica.id_ficha_fk = tbl_ficha.id_ficha\r\n"
				+ "AND tbl_tematica.id_tematica = '" + vIdTematica + "'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar todos los instructores",
			parametros = {
					
		})
		public String servicio_get_all_instructores(HttpServletRequest request, HttpServletResponse response) throws Exception {		
		
		String sql = "SELECT * FROM tbl_usuario WHERE id_rol_fk = 'ROL-becf9471'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar el usuario logeado",
			parametros = {	
					
		})
		public String servicio_get_usuario_log(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLog = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
		
		String sql = "SELECT * FROM tbl_usuario,tbl_rol\r\n"
				+ "WHERE tbl_usuario.id_rol_fk = tbl_rol.id_rol\r\n"
				+ "AND tbl_usuario.id_usuario = '" + vIdUsuarioLog + "'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para consultar usuarios por roles",
			parametros = {
					@PropiedadParametro(in = QUERY, name = "tomaAsistencia", description = "toma de asistencia a buscar", required = true, type = STRING),
					@PropiedadParametro(in = QUERY, name = "idFicha", description = "toma de asistencia a buscar", required = true, type = STRING)
					
		})
		public String servicio_get_filtro_asistencia(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String vTomaAsis = getDatoEntrada("tomaAsistencia", request);
		String vIdFicha = getDatoEntrada("idFicha", request);
		
		JSONObject infoUsuario = getInfoUserJSON(request);
		String vIdUsuarioLog = CoviLibUtils.getDataJSon(infoUsuario, "id_usuario");
		
		String sql = "SELECT * FROM tbl_asistencia,tbl_tematica,tbl_usuario\r\n"
				+ "WHERE tbl_tematica.id_instructor_fk = tbl_usuario.id_usuario\r\n"
				+ "AND tbl_asistencia.id_tematica_fk = tbl_tematica.id_tematica\r\n"
				+ "AND tbl_tematica.id_ficha_fk = '" + vIdFicha + "'\r\n"
				+ "AND tbl_asistencia.id_aprendiz_fk = '" + vIdUsuarioLog + "'\r\n"
				+ "AND tbl_asistencia.toma_asistencia = '" + vTomaAsis + "'";
		return "" + modelo.executeQuery(sql);
		
		}
	
	public static String generarContrasena(int tamano) {
		String[] listaNombres = {
	            "alatiña",
	            "beriberi",
	            "dejaelguiro",
	            "delcarajo",
	            "mamargallo",
	            "mamaderadegallo",
	            "dejalalora",
	            "voypaesa",
	            "motilar",
	            "munecaeburro",
	            "nidoeperro",
	            "nomejone",
	            "nospillamos",
	            "pasalamocha",
	            "salirpajarilla",
	            "peinarmorrocoy",
	            "trescuartos",
	            "viejomen",
	            "Behemoth",
	            "Lucifer",
	            "Satanas",
	            "Belia",
	            "Belcebu",
	            "Belfegor",
	            "Paimon",
	            "Agares",
	            "mammon",
	            "Leviatan",
	            "Lilith",
	            "Astaroth",
	            "Amon",
	            "Asmodeo",
	            "Sidragaso",
	            "Diablo",
	            "Jaldabaoth",
	            "Samael",
	            "Haakon",
	            "Gunilda",
	            "Melania",
	            "Caliope",
	            "Shakespeare ",
	            "Jamesbond"
	    };
		
		int numeroAleatorio = aleatorioRango(0, listaNombres.length -1);
		
		return generarContrasena(listaNombres [numeroAleatorio], tamano);

	}
	public static String generarContrasena(String semilla, int tamano) {
		
		//Coger una letra al azar y colocarla en mayuscala todas veces que encuentre la letra. 
	    //Resto de la semilla se regresara en minuscula.
		//Agregar un número.
		//Agregar un caracter especial.
		
		String mc = "+-*!#$%&/)=?¡¨Ñ[";
		String resultado = semilla.toLowerCase();
		int pos = aleatorioRango(0, resultado.length() -1);
		resultado = resultado.replaceAll("" + resultado.charAt(pos), ("" + resultado.charAt(pos)).toUpperCase());
		int numeroAleatorio = aleatorioRango(0, 9);
		
		resultado = numeroAleatorio + resultado;

		int cAleatorio = aleatorioRango(0, mc.length() -1);
		resultado = resultado + mc.charAt(cAleatorio);
		
		if (resultado.length() > tamano) {
			resultado = resultado.substring(0, tamano -1);
			resultado = resultado + mc.charAt(cAleatorio);
		}else if (resultado.length() < tamano) {
			while (resultado.length() < tamano) {
				if (resultado.length()% 2 == 0) {
					cAleatorio = aleatorioRango(0, mc.length() -1);
					resultado = resultado + mc.charAt(cAleatorio);
				}else {
					numeroAleatorio = aleatorioRango(0, 9);
					
					resultado = numeroAleatorio + resultado;

				}
			}
		}
		return resultado;
		
	}
	
	public static int aleatorioRango(int inferior, int superior){
        int numPosibilidades = (superior + 1) - inferior;
        double aleat = Math.random() * numPosibilidades;
        aleat = Math.floor(aleat);
        aleat = (inferior + aleat);
        return (int)aleat;
    }
	
	@PropiedadesServicio(
			metodo = GET, 
			acceso = PRIVADO, 
			descripcion = "Servicio para generar una contraseña",
			parametros = {
					
		})
		public String servicio_generate_password(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String contrasenaGen =  generarContrasena(10);
		
		JSONObject contrasena = new JSONObject();
		
		contrasena.put("contrasena", contrasenaGen);
		
		return "" + contrasena;

		}
	
	
	
	/*
	 * 
	 * 
	 * 
	 * @PropiedadesServicio( metodo = POST, acceso = PRIVADO, descripcion =
	 * "Actualizar contraseña de otro usuario", parametros = {
	 * 
	 * @PropiedadParametro(in = BODY, name = "id_user", description =
	 * "Id del usuario", required = true, type = INTEGER),
	 * 
	 * @PropiedadParametro(in = BODY, name = "new_password", description =
	 * "nueva contraseña del usuario", required = true, type = STRING) } ) public
	 * String servicio_update_user_password(HttpServletRequest request,
	 * HttpServletResponse response) throws Exception { JSONObject result = new
	 * JSONObject(); try {
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idCentro =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "id_centro"); int vIdUser =
	 * getDatoEntradaInt("id_user", request); String vPassword =
	 * getDatoEntrada("new_password", request);
	 * 
	 * boolean status = Usuario.actualizarContrasena(modeloMySQl, vIdUser,
	 * vPassword, idCentro);
	 * 
	 * CoviLibUtils.setJson(result, "RESULT", status);
	 * 
	 * if(!status) { return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "Error de seguridad", response, result); }
	 * 
	 * }catch (Exception e) { return getError(HttpServletResponse.SC_UNAUTHORIZED,
	 * "Error de seguridad", response, result); } return "" + result; }
	 * 
	 * @PropiedadesServicio( metodo = POST, acceso = PRIVADO, descripcion =
	 * "Actualizar el estado de un usuario", parametros = {
	 * 
	 * @PropiedadParametro(in = BODY, name = "id_user", description =
	 * "Id del usuario", required = true, type = INTEGER),
	 * 
	 * @PropiedadParametro(in = BODY, name = "new_status", description =
	 * "nuevo estado del usuario", required = true, type = TipoDato.LISTA,
	 * listaValores = {"H", "I"}) } ) public String
	 * servicio_update_user_status(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception { JSONObject result = new JSONObject(); try {
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idCentro =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "id_centro"); int vIdUser =
	 * getDatoEntradaInt("id_user", request); String vEstado =
	 * getDatoEntrada("new_status", request);
	 * 
	 * boolean status = Usuario.actualizarEstado(modeloMySQl, vIdUser, vEstado,
	 * idCentro);
	 * 
	 * CoviLibUtils.setJson(result, "RESULT", status);
	 * 
	 * if(!status) { return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "Error de seguridad", response, result); }
	 * 
	 * }catch (Exception e) { return getError(HttpServletResponse.SC_UNAUTHORIZED,
	 * "Error de seguridad", response, result); } return "" + result; }
	 * 
	 * 
	 * @PropiedadesServicio( metodo = POST, acceso = PRIVADO, descripcion =
	 * "Actualizar contraseña propia", parametros = {
	 * 
	 * @PropiedadParametro(in = BODY, name = "new_password", description =
	 * "nueva contraseña", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "old_password", description =
	 * "nueva contraseña", required = true, type = STRING) } ) public String
	 * servicio_update_password(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception { JSONObject result = new JSONObject(); try {
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idCentro =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "id_centro"); int idUsuario =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "codigo_usuario");
	 * 
	 * String oldPassword = getDatoEntrada("old_password", request);
	 * 
	 * String dbPass = Usuario.generarContrasena(modeloMySQl, idUsuario,
	 * oldPassword);
	 * 
	 * String sql = "select * from " + ObjectOrm.obtenerNombreTabla(Usuario.class) +
	 * " where "; sql += " `clavesegura` = '" + dbPass + "' "; sql +=
	 * " AND `codigo_usuario` = '" + idUsuario + "' AND `id_centro` = " + idCentro;
	 * sql += " AND estado = '" + EstadoUsuario.HABILITADO + "'";
	 * 
	 * JSONArray consulta = modeloMySQl.executeQuery(sql); if(consulta.length() <=
	 * 0) { return getError(HttpServletResponse.SC_UNAUTHORIZED,
	 * "Contraseña anterior invalida", response, result); }
	 * 
	 * String vPassword = getDatoEntrada("new_password", request);
	 * 
	 * boolean status = Usuario.actualizarContrasena(modeloMySQl,
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "codigo_usuario"), vPassword,
	 * idCentro);
	 * 
	 * CoviLibUtils.setJson(result, "RESULT", status);
	 * 
	 * if(!status) { return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "Error de seguridad", response, result); }
	 * 
	 * }catch (Exception e) { return getError(HttpServletResponse.SC_UNAUTHORIZED,
	 * "Error de seguridad", response, result); } return "" + result; }
	 * 
	 * 
	 * 
	 * @Override
	 * 
	 * @PropiedadesServicio( metodo = GET, acceso = PUBLICO, descripcion =
	 * "EndPoint para saber si el servicio esta OK" ) public String
	 * servicio_status(HttpServletRequest request, HttpServletResponse response)
	 * throws Exception { Connection conexion = null; JSONObject result = new
	 * JSONObject();
	 * 
	 * boolean statusMysql = false; try {
	 * 
	 * CoviLibUtils.setJson(result, "AMBIENTE", getConfig().getAmbiente());
	 * 
	 * conexion = modeloMySQl.obtenerConexion(); statusMysql = false;
	 * 
	 * if (conexion != null && !conexion.isClosed()) { statusMysql = true;
	 * CoviLibUtils.setJson(result, "ESTADO_CONEXION_MYSQL", statusMysql);
	 * 
	 * }else { CoviLibUtils.setJson(result, "ESTADO_CONEXION_MYSQL", statusMysql);
	 * return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "No hay conexion con la base de datos", response, result); }
	 * 
	 *
	 * } catch (Exception ex) { statusMysql = false;
	 * CoviLog.escribirLogError(getClass(), "getConnectionJBOSS", ex);
	 * CoviLibUtils.setJson(result, "ESTADO_CONEXION_MYSQL", statusMysql); return
	 * getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "No hay conexion con la base de datos", response, result); } finally {
	 * ModeloOrm.cerrarConexion(conexion, null); }
	 * 
	 * return "" + result;
	 * 
	 * }
	 * 
	 * public int getRol(String strRol) { int rolCliente =
	 * CoviLibUtils.getDataJSonInt(getConfig().getInfo(), "roles.cliente_rana"); int
	 * rolAdmin = CoviLibUtils.getDataJSonInt(getConfig().getInfo(),
	 * "roles.admin_rana"); switch (strRol) { case "ADMIN": return rolAdmin; }
	 * return rolCliente; }
	 * 
	 * @Override public boolean tienePermisos(String token, String nombreServicio)
	 * throws Exception { //int rolCliente =
	 * CoviLibUtils.getDataJSonInt(getConfig().getInfo(), "roles.cliente_rana"); int
	 * rolAdmin = CoviLibUtils.getDataJSonInt(getConfig().getInfo(),
	 * "roles.admin_rana");
	 * 
	 * JSONObject infoUserSession = JWT.getInfoUserJSON(token, getSecret()); //int
	 * idUsuario = CoviLibUtils.getDataJSonInt(infoUserSession, "codigo_usuario");
	 * int idRol = CoviLibUtils.getDataJSonInt(infoUserSession, "idRol");
	 * 
	 * if(idRol != rolAdmin) {
	 * if(nombreServicio.equalsIgnoreCase("servicio_save_recarga")
	 * ||nombreServicio.equalsIgnoreCase("servicio_save_user")
	 * ||nombreServicio.equalsIgnoreCase("servicio_get_all_users")
	 * ||nombreServicio.equalsIgnoreCase("servicio_update_user_status")
	 * ||nombreServicio.equalsIgnoreCase("servicio_update_user_password")
	 * ||nombreServicio.equalsIgnoreCase("servicio_get_all_recargas")
	 * ||nombreServicio.equalsIgnoreCase("servicio_modify_user")
	 * ||nombreServicio.equalsIgnoreCase("servicio_get_user")
	 * //||nombreServicio.equalsIgnoreCase("servicio_")
	 * //||nombreServicio.equalsIgnoreCase("servicio_")
	 * //||nombreServicio.equalsIgnoreCase("servicio_")
	 * //||nombreServicio.equalsIgnoreCase("servicio_") ) { return false; } }
	 * 
	 * return true;
	 * 
	 * }
	 * 
	 * @PropiedadesServicio( metodo = GET, acceso = PRIVADO, descripcion =
	 * "Retornar todos los usuarios" ) public String
	 * servicio_get_all_users(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception {
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idCentro =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "id_centro"); int rolCliente =
	 * CoviLibUtils.getDataJSonInt(getConfig().getInfo(), "roles.cliente_rana"); int
	 * rolAdmin = CoviLibUtils.getDataJSonInt(getConfig().getInfo(),
	 * "roles.admin_rana");
	 * 
	 * 
	 * String campos =
	 * "identificacion, codigo_usuario, nombres, apellidos, nick, correo, estado, idRol, telFijo, telMovil, id_centro"
	 * ; String sql = "SELECT " + campos + " FROM " +
	 * ObjectOrm.obtenerNombreTabla(Usuario.class);
	 * 
	 * sql += " where (idRol = " + rolAdmin + " OR idRol = " + rolCliente +
	 * ") AND id_centro = " + idCentro;
	 * 
	 * return "" + modeloMySQl.executeQuery(sql); }
	 * 
	 * @PropiedadesServicio( metodo = GET, acceso = PRIVADO, descripcion =
	 * "Retornar la informacion de un usuario", parametros = {
	 * 
	 * @PropiedadParametro(in = BODY, name = "id_usuario", description =
	 * "Id usuario al que se le van a modificar los datos", required = true, type =
	 * INTEGER) } ) public String servicio_get_user(HttpServletRequest request,
	 * HttpServletResponse response) throws Exception {
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idCentro =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "id_centro"); int rolCliente =
	 * CoviLibUtils.getDataJSonInt(getConfig().getInfo(), "roles.cliente_rana"); int
	 * rolAdmin = CoviLibUtils.getDataJSonInt(getConfig().getInfo(),
	 * "roles.admin_rana");
	 * 
	 * long idUsuario = getDatoEntradaLong("id_usuario", request);
	 * 
	 * 
	 * String campos =
	 * "identificacion, codigo_usuario, nombres, apellidos, nick, correo, estado, idRol, telFijo, telMovil, id_centro"
	 * ; String sql = "SELECT " + campos + " FROM " +
	 * ObjectOrm.obtenerNombreTabla(Usuario.class);
	 * 
	 * sql += " where (idRol = " + rolAdmin + " OR idRol = " + rolCliente +
	 * ") AND id_centro = " + idCentro + " AND codigo_usuario =" + idUsuario;
	 * 
	 * JSONArray consulta = modeloMySQl.executeQuery(sql);
	 * 
	 * if (consulta.length() < 1) { return
	 * getError(HttpServletResponse.SC_NOT_FOUND, "No existe el usuario", response);
	 * }
	 * 
	 * return "" + consulta.get(0);
	 * 
	 * }
	 * 
	 * @PropiedadesServicio( metodo = GET, acceso = PRIVADO, descripcion =
	 * "Retornar todos las activaciones de un usuario" ) public String
	 * servicio_get_my_activaciones(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception {
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idUsuario =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "codigo_usuario");
	 * 
	 * 
	 * String campos = "*"; String sql = "SELECT " + campos + " FROM " +
	 * ObjectOrm.obtenerNombreTabla(ActivacionRana.class);
	 * 
	 * sql += " where id_usuario_fk2 = " + idUsuario +
	 * " ORDER BY fecha_hora_so DESC";
	 * 
	 * JSONArray items = modeloMySQl.executeQuery(sql);
	 * 
	 * for(int i = 0; i < items.length(); i++) { JSONObject obj =
	 * items.getJSONObject(i); obj.remove("fecha_hora_pc");
	 * obj.remove("semilla_generada"); }
	 * 
	 * return "" + items;
	 * 
	 * }
	 * 
	 * @PropiedadesServicio( metodo = GET, acceso = PRIVADO, descripcion =
	 * "Retornar todos los usuarios" ) public String
	 * servicio_get_all_recargas(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception {
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idCentro =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "id_centro");
	 * 
	 * String campos =
	 * "nombres, id_recarga, apellidos, nick, id_usuario_fk, fecha_hora, id_usuario_creador, numero_licencias, tipo_licencia, licencias_restantes"
	 * ; String sql = "SELECT " + campos + " FROM " +
	 * ObjectOrm.obtenerNombreTabla(Recarga.class) + "," +
	 * ObjectOrm.obtenerNombreTabla(Usuario.class);
	 * 
	 * sql += " where codigo_usuario = id_usuario_fk AND id_centro = " + idCentro;
	 * 
	 * return "" + modeloMySQl.executeQuery(sql); }
	 * 
	 * @PropiedadesServicio( metodo = POST, acceso = PRIVADO, descripcion =
	 * "Agregar nuevo usuario", parametros = {
	 * 
	 * @PropiedadParametro(in = BODY, name = "identificacion", description =
	 * "Cedula", required = true, type = INTEGER),
	 * 
	 * @PropiedadParametro(in = BODY, name = "nombres", description =
	 * "Nombre del usuario", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "apellidos", description =
	 * "Apellidos del usuario", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "nick", description =
	 * "Nick del usuario", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "correo", description =
	 * "cuenta de correo", required = true, type = STRING, format = CORREO),
	 * 
	 * @PropiedadParametro(in = BODY, name = "rol", description =
	 * "Rol que se le asignara", required = true, type = LISTA, listaValores =
	 * {"ADMIN", "CLIENTE"}),
	 * 
	 * @PropiedadParametro(in = BODY, name = "tel_fijo", description = "Telefono 1",
	 * required = true, type = STRING, format = TELEFONO),
	 * 
	 * @PropiedadParametro(in = BODY, name = "tel_movil", description =
	 * "Telefono 2", required = false, type = STRING, format = TELEFONO),
	 * 
	 * @PropiedadParametro(in = BODY, name = "direccion", description =
	 * "Direccion de residencia", required = true, type = STRING) } ) public String
	 * servicio_save_user(HttpServletRequest request, HttpServletResponse response)
	 * throws Exception {
	 * 
	 * try { int idRol = getRol(getDatoEntrada("rol", request)); String vNick =
	 * getDatoEntrada("nick", request); long vIdentificacion =
	 * getDatoEntradaLong("identificacion", request); usuario = new
	 * Usuario(modeloMySQl); usuario =
	 * (Usuario)usuario.buscarElemento(" identificacion = " + vIdentificacion);
	 * if(usuario != null) { return getError(HttpServletResponse.SC_FOUND,
	 * "El numero de identificación del usuario ya esta registrado", response); }
	 * 
	 * usuario = new Usuario(modeloMySQl); usuario =
	 * (Usuario)usuario.buscarElemento(" nick = '" + vNick + "'"); if(usuario !=
	 * null) { return getError(HttpServletResponse.SC_FOUND,
	 * "El nick del usuario ya esta registrado", response); }
	 * 
	 * usuario = new Usuario(modeloMySQl); JSONObject infoUserSession =
	 * getInfoUserJSON(request); int idCentro =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "id_centro");
	 * 
	 * String vNombres = getDatoEntrada("nombres", request); String vApellidos =
	 * getDatoEntrada("apellidos", request); String vCorreo =
	 * getDatoEntrada("correo", request); String vTelFijo =
	 * getDatoEntrada("tel_fijo", request); String vTelMovil =
	 * getDatoEntrada("tel_movil", request); String vDireccion =
	 * getDatoEntrada("direccion", request); String pIdUnico =
	 * CoviLibUtils.generateIdUUID(usuario.obtenerAnotaciones().prefijoId());
	 * 
	 * 
	 * usuario.setNombres(vNombres); usuario.setApellidos(vApellidos);
	 * usuario.setNick(vNick); usuario.setCorreo(vCorreo);
	 * usuario.setIdCentro(idCentro); usuario.setIdRol(idRol);
	 * usuario.setIdentificacion(vIdentificacion); usuario.setTelFijo(vTelFijo);
	 * usuario.setTelMovil(vTelMovil); usuario.setEstado("" +
	 * EstadoUsuario.HABILITADO); usuario.setIdUnicoUsuario(pIdUnico);
	 * usuario.setDescripcionBaja(vDireccion);
	 * 
	 * usuario = (Usuario)usuario.guardar();
	 * 
	 * String vPassword = CoviLibUtils.generateIdUUID("PaS");
	 * 
	 * Usuario.actualizarContrasena(modeloMySQl, usuario.getCodigoUsuario(),
	 * vPassword, idCentro);
	 * 
	 * return "" + usuario;
	 * 
	 * }catch (Exception e) {
	 * 
	 * CoviLog.escribirLogError(getClass(), "servicio_save_user", e);
	 * 
	 * return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "Error interno - no se pudo guardar el nuevo usuario", response);
	 * 
	 * }
	 * 
	 * 
	 * }
	 * 
	 * @PropiedadesServicio( metodo = POST, acceso = PRIVADO, descripcion =
	 * "Modificar usuario", parametros = {
	 * 
	 * @PropiedadParametro(in = BODY, name = "id_usuario", description =
	 * "Id usuario al que se le van a modificar los datos", required = true, type =
	 * INTEGER),
	 * 
	 * @PropiedadParametro(in = BODY, name = "identificacion", description =
	 * "Cedula", required = true, type = INTEGER),
	 * 
	 * @PropiedadParametro(in = BODY, name = "nombres", description =
	 * "Nombre del usuario", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "apellidos", description =
	 * "Apellidos del usuario", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "nick", description =
	 * "Nick del usuario", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "correo", description =
	 * "cuenta de correo", required = true, type = STRING, format = CORREO),
	 * 
	 * @PropiedadParametro(in = BODY, name = "rol", description =
	 * "Rol que se le asignara", required = true, type = LISTA, listaValores =
	 * {"ADMIN", "CLIENTE"}),
	 * 
	 * @PropiedadParametro(in = BODY, name = "tel_fijo", description = "Telefono 1",
	 * required = true, type = STRING, format = TELEFONO),
	 * 
	 * @PropiedadParametro(in = BODY, name = "tel_movil", description =
	 * "Telefono 2", required = false, type = STRING, format = TELEFONO),
	 * 
	 * @PropiedadParametro(in = BODY, name = "direccion", description =
	 * "Direccion de residencia", required = true, type = STRING) } ) public String
	 * servicio_modify_user(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception {
	 * 
	 * try {
	 * 
	 * int idUserFk = getDatoEntradaInt("id_usuario", request); Usuario
	 * usuarioAModificar = new Usuario(modeloMySQl); usuarioAModificar =
	 * (Usuario)usuarioAModificar.buscarElemento(" codigo_usuario = " + idUserFk);
	 * if(usuarioAModificar == null) { return
	 * getError(HttpServletResponse.SC_NOT_FOUND, "El usuario no existe", response);
	 * }
	 * 
	 * int idRol = getRol(getDatoEntrada("rol", request)); String vNick =
	 * getDatoEntrada("nick", request); long vIdentificacion =
	 * getDatoEntradaLong("identificacion", request); usuario = new
	 * Usuario(modeloMySQl); usuario =
	 * (Usuario)usuario.buscarElemento(" identificacion = " + vIdentificacion +
	 * " AND codigo_usuario <> " + idUserFk); if(usuario != null) { return
	 * getError(HttpServletResponse.SC_FOUND,
	 * "El numero de identificación del usuario ya esta registrado", response); }
	 * 
	 * usuario = new Usuario(modeloMySQl); usuario =
	 * (Usuario)usuario.buscarElemento(" nick = '" + vNick + "'" +
	 * " AND codigo_usuario <> " + idUserFk); if(usuario != null) { return
	 * getError(HttpServletResponse.SC_FOUND,
	 * "El nick del usuario ya esta registrado", response); }
	 * 
	 * String vNombres = getDatoEntrada("nombres", request); String vApellidos =
	 * getDatoEntrada("apellidos", request); String vCorreo =
	 * getDatoEntrada("correo", request); String vTelFijo =
	 * getDatoEntrada("tel_fijo", request); String vTelMovil =
	 * getDatoEntrada("tel_movil", request); String vDireccion =
	 * getDatoEntrada("direccion", request);
	 * 
	 * usuarioAModificar.setNombres(vNombres);
	 * usuarioAModificar.setApellidos(vApellidos); usuarioAModificar.setNick(vNick);
	 * usuarioAModificar.setCorreo(vCorreo); usuarioAModificar.setIdRol(idRol);
	 * usuarioAModificar.setIdentificacion(vIdentificacion);
	 * usuarioAModificar.setTelFijo(vTelFijo);
	 * usuarioAModificar.setTelMovil(vTelMovil);
	 * usuarioAModificar.setDescripcionBaja(vDireccion);
	 * 
	 * boolean resultUpdate = usuarioAModificar.actualizar(" codigo_usuario = " +
	 * idUserFk);
	 * 
	 * JSONObject result = usuarioAModificar.toJson();
	 * 
	 * CoviLibUtils.setJson(result, "modificado", resultUpdate);
	 * 
	 * return "" + result;
	 * 
	 * }catch (Exception e) {
	 * 
	 * CoviLog.escribirLogError(getClass(), "servicio_save_user", e);
	 * 
	 * return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "Error interno - no se pudo guardar el nuevo usuario", response);
	 * 
	 * }
	 * 
	 * 
	 * }
	 * 
	 * @PropiedadesServicio( metodo = POST, acceso = PRIVADO, descripcion =
	 * "Agregar una nueva recarga", parametros = {
	 * 
	 * @PropiedadParametro(in = BODY, name = "id_usuario_fk", description =
	 * "Id usuario al que se le va a realizar la recarga", required = true, type =
	 * INTEGER),
	 * 
	 * @PropiedadParametro(in = BODY, name = "numero_licencias", description =
	 * "Numero de licencias", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "tipo_licencia", description =
	 * "Tipo de Licencia", required = true, type = LISTA, listaValores = {"RANA_PI",
	 * "RANA_ANDROID"}), } ) public String servicio_save_recarga(HttpServletRequest
	 * request, HttpServletResponse response) throws Exception {
	 * 
	 * try {
	 * 
	 * int idUserFk = getDatoEntradaInt("id_usuario_fk", request); Usuario usuario =
	 * new Usuario(modeloMySQl); usuario =
	 * (Usuario)usuario.buscarElemento(" codigo_usuario = " + idUserFk); if(usuario
	 * == null) { return getError(HttpServletResponse.SC_NOT_FOUND,
	 * "El usuario no existe", response); }
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idUsuario =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "codigo_usuario"); String
	 * tipoLicencia = getDatoEntrada("tipo_licencia", request); int numeroLicencias
	 * = getDatoEntradaInt("numero_licencias", request);
	 * 
	 * Recarga recarga = new Recarga(modeloMySQl); recarga.setIdUsuariofk(idUserFk);
	 * recarga.setFechaHora(CoviFecha.ahora());
	 * recarga.setIdUsuarioCreador(idUsuario);
	 * recarga.setTipoLicencia(tipoLicencia);
	 * recarga.setLicenciasRestantes(numeroLicencias);
	 * recarga.setNumeroLicencias(numeroLicencias);
	 * 
	 * recarga.guardar();
	 * 
	 * return "" + recarga;
	 * 
	 * }catch (Exception e) {
	 * 
	 * CoviLog.escribirLogError(getClass(), "servicio_save_recarga", e);
	 * 
	 * return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "Error interno - no se pudo guardar la recarga", response);
	 * 
	 * } }
	 * 
	 * 
	 * 
	 * @PropiedadesServicio( metodo = GET, acceso = PRIVADO, descripcion =
	 * "Dash board del usuario" ) public String
	 * servicio_my_dashboard(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception {
	 * 
	 * JSONObject result = new JSONObject();
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idUsuario =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "codigo_usuario");
	 * 
	 * int noRanaPi = 0; int noRanaAndroid = 0;
	 * 
	 * String sql12 = "SELECT "; sql12 += "(select SUM(licencias_restantes) from " +
	 * ObjectOrm.obtenerNombreTabla(Recarga.class) + " where id_usuario_fk = " +
	 * idUsuario + " AND tipo_licencia = 'RANA_PI') as totalr_pi,"; sql12 +=
	 * "(select SUM(licencias_restantes) from " +
	 * ObjectOrm.obtenerNombreTabla(Recarga.class) + " where id_usuario_fk = " +
	 * idUsuario + " AND tipo_licencia = 'RANA_ANDROID') as totalr_and"; JSONArray
	 * totales = modeloMySQl.executeQuery(sql12); if(totales.length() > 0) {
	 * JSONObject temp = totales.getJSONObject(0); noRanaPi =
	 * CoviLibUtils.getDataJSonInt(temp, "totalr_pi"); noRanaAndroid =
	 * CoviLibUtils.getDataJSonInt(temp, "totalr_and"); }
	 * 
	 * 
	 * String campos = "*"; String sql = "SELECT " + campos + " FROM " +
	 * ObjectOrm.obtenerNombreTabla(ActivacionRana.class);
	 * 
	 * sql += " where id_usuario_fk2 = " + idUsuario +
	 * " ORDER BY fecha_hora_so DESC LIMIT 10";
	 * 
	 * JSONArray items = modeloMySQl.executeQuery(sql);
	 * 
	 * for(int i = 0; i < items.length(); i++) { JSONObject obj =
	 * items.getJSONObject(i); obj.remove("fecha_hora_pc");
	 * obj.remove("semilla_generada"); }
	 * 
	 * CoviLibUtils.setJson(result, "no_rana_pi", noRanaPi);
	 * CoviLibUtils.setJson(result, "no_rana_android", noRanaAndroid);
	 * CoviLibUtils.setJson(result, "ultimas_act", items);
	 * 
	 * return "" + result;
	 * 
	 * }
	 * 
	 * @PropiedadesServicio( metodo = POST, acceso = PRIVADO, descripcion =
	 * "Activar nueva llave", parametros = {
	 * 
	 * @PropiedadParametro(in = FORM_DATA, name = "file", description =
	 * "Archivo de carga", required = false, type = FILE),
	 * 
	 * @PropiedadParametro(in = QUERY, name = "tipo_licencia", description =
	 * "Tipo de Licencia", required = true, type = LISTA, listaValores = {"RANA_PI",
	 * "RANA_ANDROID"}), } ) public String
	 * servicio_activar_fichero(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception {
	 * 
	 * Archivo archivo;
	 * 
	 * try {
	 * 
	 * CoviUpload subir = new CoviUpload(); JSONObject JO =
	 * subir.SubirArchivo(request);
	 * 
	 * archivo = new
	 * Archivo(JO.getJSONArray("archivos").getJSONObject(0).getString("ruta"));
	 * 
	 * String infoQr = QRCode.readQRCode(archivo.getPath());
	 * 
	 * request.setAttribute("llave", infoQr);
	 * 
	 * return servicio_activar(request, response);
	 * 
	 * }catch (Exception e) {
	 * 
	 * return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "No se pudo leer el QR", response);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * @PropiedadesServicio( metodo = POST, acceso = PRIVADO, descripcion =
	 * "Activar nueva llave", parametros = {
	 * 
	 * @PropiedadParametro(in = BODY, name = "nombreOriginal", description =
	 * "Nombre original del archivo", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "tipo_licencia", description =
	 * "Tipo de Licencia", required = true, type = LISTA, listaValores = {"RANA_PI",
	 * "RANA_ANDROID"}),
	 * 
	 * @PropiedadParametro(in = BODY, name = "infoArchivo", description =
	 * "Contenido del archivo", required = true, type = STRING) } ) public String
	 * servicio_activar_base64(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception {
	 * 
	 * try {
	 * 
	 * String infoArchivo = getDatoEntrada("infoArchivo", request); String[] vector
	 * = infoArchivo.split(","); String base64String = infoArchivo;
	 * 
	 * if(vector.length > 1) { base64String = vector[1]; }
	 * if(!CoviBase64Utils.isBase64Valid(base64String)) { return
	 * CoviOperacionesServicios.getError(HttpServletResponse.SC_BAD_REQUEST,
	 * "Data del logo inválido", response); }
	 * 
	 * String pNombreOriginal = getDatoEntrada("nombreOriginal", request);
	 * 
	 * String vRuta = getConfig().getDataPath("configuracion.ruta_temp");
	 * ManejadorArchivos.validaYCreaDirectorio(vRuta);
	 * 
	 * Archivo archivoOriginal = new Archivo(pNombreOriginal);
	 * 
	 * String rutaNombreFinalArchivo =
	 * ManejadorArchivos.encontarNombreArchivoQNoExista(vRuta +
	 * archivoOriginal.getNombreArchivoSinExtension(),
	 * archivoOriginal.getExtension());
	 * 
	 * CoviBase64Utils.base64StringToArchivo(infoArchivo, rutaNombreFinalArchivo);
	 * 
	 * String infoQr = QRCode.readQRCode(rutaNombreFinalArchivo);
	 * 
	 * request.setAttribute("llave", infoQr);
	 * 
	 * return servicio_activar(request, response);
	 * 
	 * }catch (Exception e) {
	 * 
	 * return getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "No se pudo leer el QR", response);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * @PropiedadesServicio( metodo = POST, acceso = PRIVADO, descripcion =
	 * "Activar nueva llave", parametros = {
	 * 
	 * @PropiedadParametro(in = BODY, name = "llave", description =
	 * "Llave a Activar", required = true, type = STRING),
	 * 
	 * @PropiedadParametro(in = BODY, name = "tipo_licencia", description =
	 * "Tipo de Licencia", required = true, type = LISTA, listaValores = {"RANA_PI",
	 * "RANA_ANDROID"}), } ) public String servicio_activar(HttpServletRequest
	 * request, HttpServletResponse response) throws Exception { JSONObject result =
	 * new JSONObject(); try {
	 * 
	 * JSONArray activaciones = new JSONArray();
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idUsuario =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "codigo_usuario");
	 * TipoActivacion tipoActivacion = TipoActivacion.RANA_PI; String dirmac;
	 * 
	 * Usuario usuario = new Usuario(modeloMySQl); usuario =
	 * (Usuario)usuario.buscarElemento(" codigo_usuario = " + idUsuario); if(usuario
	 * == null) { return getError(HttpServletResponse.SC_NOT_FOUND,
	 * "El id de usuario " + idUsuario + " no existe", response); }
	 * 
	 * Recarga recarga = new Recarga(modeloMySQl); String tipoLicencia =
	 * getDatoEntrada("tipo_licencia", request); recarga =
	 * (Recarga)recarga.buscarElemento(" id_usuario_fk = " + idUsuario +
	 * " AND tipo_licencia = '" + tipoLicencia + "' AND licencias_restantes > 0");
	 * if(recarga == null || recarga.getLicenciasRestantes() < 1) { return
	 * getError(HttpServletResponse.SC_NOT_FOUND,
	 * "El usuario no tiene recargas para este tipo de licencias", response); }
	 * 
	 * // Activar
	 * 
	 * String idunicoRana = getDatoEntrada("llave", request);
	 * if(tipoLicencia.equalsIgnoreCase("RANA_ANDROID")) { dirmac =
	 * idunicoRana.substring(2); tipoActivacion = TipoActivacion.RANA_ANDROID; }else
	 * { dirmac = SeguridadRana.sacarMacDeIdUnico(idunicoRana); }
	 * 
	 * //MacRana mac1 = (MacRana) new
	 * MacRana().buscarXWherePrimerElemento(" mac_rana = '" + dirmac + "'"); MacRana
	 * mac1 = new MacRana(modeloMySQl); mac1 =
	 * (MacRana)mac1.buscarElemento(" mac_rana = '" + dirmac + "'");
	 * 
	 * java.sql.Timestamp ya22 = CoviFecha.ahoraConZonaHoraria();
	 * 
	 * //long numeroActivaciones = 0; String fechaPrimeraActivacion = "" + ya22;
	 * String fechaAnteriorActivacion = "" + ya22; int vIdMac = 0;
	 * 
	 * if(mac1 == null) { mac1 = new MacRana(modeloMySQl);
	 * mac1.setIdUsuarioFk(idUsuario); mac1.setFechaHoraSo(ya22);
	 * mac1.setMacRana(dirmac); mac1 = (MacRana)mac1.guardar(); vIdMac =
	 * mac1.getIdMac(); }else{
	 * 
	 * vIdMac = mac1.getIdMac();
	 * 
	 * String sql = "select * from " +
	 * ObjectOrm.obtenerNombreTabla(ActivacionRana.class) +
	 * " where id_usuario_fk2 = " + idUsuario + " AND id_mac_rana=" +
	 * mac1.getIdMac(); activaciones = modeloMySQl.executeQuery(sql);
	 * 
	 * String sqlPrimeraAct = " where id_usuario_fk2 = " + idUsuario +
	 * " AND fecha_hora_so = (SELECT min(fecha_hora_so) FROM tbl_activacion_rana where id_usuario_fk2 = "
	 * + idUsuario + " AND id_mac_rana=" + mac1.getIdMac() + ")";
	 * 
	 * String sqlAnteriorAct = " where id_usuario_fk2 = " + idUsuario +
	 * " AND fecha_hora_so = (SELECT max(fecha_hora_so) FROM tbl_activacion_rana where id_usuario_fk2 = "
	 * + idUsuario + " AND id_mac_rana=" + mac1.getIdMac() + ")";
	 * 
	 * ActivacionRana primeraActivacion = new ActivacionRana(modeloMySQl);
	 * primeraActivacion = (ActivacionRana) primeraActivacion.buscarElemento(
	 * sqlPrimeraAct );
	 * 
	 * ActivacionRana anteriorActivacion = new ActivacionRana(modeloMySQl);
	 * anteriorActivacion = (ActivacionRana) anteriorActivacion.buscarElemento(
	 * sqlAnteriorAct );
	 * 
	 * if(primeraActivacion != null){ fechaPrimeraActivacion = "" +
	 * primeraActivacion.getFechaHoraSo(); }
	 * 
	 * if (anteriorActivacion != null){ fechaAnteriorActivacion = "" +
	 * anteriorActivacion.getFechaHoraSo(); }
	 * 
	 * }
	 * 
	 * String semilla22; if (tipoActivacion == TipoActivacion.RANA_ANDROID) {
	 * semilla22 = SeguridadRana.generaSemillaAndroid(idunicoRana); }else {
	 * semilla22 = SeguridadRana.sacarSemilla(idunicoRana); }
	 * 
	 * ActivacionRana nuevaAct = new ActivacionRana(modeloMySQl);
	 * 
	 * nuevaAct.setIdMacRana(vIdMac); nuevaAct.setFechaHoraSo(ya22); if
	 * (tipoActivacion == TipoActivacion.RANA_ANDROID) { nuevaAct.setFechaHoraPc(""
	 * + CoviFecha.ahora()); }else {
	 * nuevaAct.setFechaHoraPc(SeguridadRana.sacarfechaHoraDeIdUnico(idunicoRana));
	 * } nuevaAct.setIdUnicoPc(idunicoRana); nuevaAct.setSemillaGenerada(semilla22);
	 * nuevaAct.setIdUsuarioFk2(idUsuario);
	 * 
	 * nuevaAct = (ActivacionRana)nuevaAct.guardar();
	 * 
	 * String vClave; if (tipoActivacion == TipoActivacion.RANA_ANDROID) { vClave =
	 * SeguridadRana.generaClaveAndroid(dirmac); }else { vClave =
	 * CoviLibUtils.lpad("" + nuevaAct.getIdActivacionRana(), "0", 4) + semilla22 +
	 * CoviLibUtils.lpad("" + idUsuario, "0", 4); }
	 * 
	 * // Disminuir la licencia String nuevaOReactivacion = "REACTIVACION"; int
	 * licRestantes = recarga.getLicenciasRestantes();
	 * 
	 * if(activaciones.length() < 1) {
	 * recarga.setLicenciasRestantes(recarga.getLicenciasRestantes() - 1);
	 * if(!recarga.actualizar(" id_recarga = '" + recarga.getIdRecarga() + "'" )) {
	 * throw new
	 * OperacionesServiciosException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "No se pudo actializar las licencias"); } nuevaOReactivacion = "NUEVA";
	 * licRestantes --; }
	 * 
	 * for(int i = 0; i < activaciones.length(); i++) { JSONObject obj =
	 * activaciones.getJSONObject(i); obj.remove("id_unico_pc");
	 * obj.remove("semilla_generada"); obj.remove("id_usuario_fk2");
	 * obj.remove("fecha_hora_pc"); }
	 * 
	 * result.put("guardado", vClave); result.put("numero_activaciones",
	 * activaciones.length()); result.put("fecha_primera_activacion",
	 * fechaPrimeraActivacion); result.put("fecha_anterior_activacion",
	 * fechaAnteriorActivacion); result.put("activaciones", activaciones);
	 * 
	 * result.put("tipo_activacion", nuevaOReactivacion);
	 * result.put("licencias_restantes", licRestantes); result.put("tipo_licencia",
	 * tipoLicencia);
	 * 
	 * }catch(OperacionesServiciosException err){ return getError(err, response);
	 * }catch(Exception err){ return
	 * getError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	 * "No se pudo generar la activacion", response); }
	 * 
	 * return "" + result;
	 * 
	 * }
	 * 
	 * @PropiedadesServicio( metodo = GET, acceso = PRIVADO, descripcion =
	 * "Obtener las acciones del menu de usuarios" ) public String
	 * servicio_acciones_usuario(HttpServletRequest request, HttpServletResponse
	 * response) throws Exception {
	 * 
	 * JSONArray result = new JSONArray();
	 * 
	 * JSONObject infoUserSession = getInfoUserJSON(request); int idRol =
	 * CoviLibUtils.getDataJSonInt(infoUserSession, "idRol");
	 * 
	 * int rolCliente = CoviLibUtils.getDataJSonInt(getConfig().getInfo(),
	 * "roles.cliente_rana"); int rolAdmin =
	 * CoviLibUtils.getDataJSonInt(getConfig().getInfo(), "roles.admin_rana");
	 * if(idRol == rolAdmin) { result.put(getAccion("Usuarios", "fas fa-users",
	 * "usuarios.html")); result.put(getAccion("Ventas",
	 * "fas fa-file-invoice-dollar", "ventas.html"));
	 * result.put(getAccion("Activar", "fas fa-key", "activar_admin.html"));
	 * result.put(getAccion("Activar QR", "fas fa-key", "activar_admin_qr.html"));
	 * // result.put(getAccion("Buscar", "fas fa-search", "buscar.html"));
	 * result.put(getAccion("Contraseña", "fas fa-unlock-alt", "contrasena.html"));
	 * }else if(idRol == rolCliente) { result.put(getAccion("Activar", "fas fa-key",
	 * "activar.html")); result.put(getAccion("Activar QR", "fas fa-key",
	 * "activar_qr.html")); result.put(getAccion("Activaciones", "fas fa-search",
	 * "activaciones.html")); result.put(getAccion("contraseña",
	 * "fas fa-unlock-alt", "contrasena.html")); }else { return
	 * getError(HttpServletResponse.SC_UNAUTHORIZED, "sesión no válida", response);
	 * }
	 * 
	 * return "" + result; }
	 * 
	 * public static JSONObject getAccion(String label, String icono, String pagina)
	 * { JSONObject result = new JSONObject(); CoviLibUtils.setJson(result, "label",
	 * label); CoviLibUtils.setJson(result, "icono", icono);
	 * CoviLibUtils.setJson(result, "pagina", pagina); return result; }
	 */

}