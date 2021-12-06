package entities;


import static com.covilib.orm.ModeloOrm.TipoDatoORM.TEXTO;

import org.json.JSONObject;

import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
import com.covilib.orm.annotations.ColumnaAnotaciones;
import com.covilib.orm.annotations.TablaAnotaciones;
import com.covilib.security.EncriptadorAES;

@TablaAnotaciones(nombreTabla = "tbl_usuario", prefijoId = "USER")
public class Usuario extends ObjectOrm {
	
	public Usuario(JSONObject rs, ModeloOrm pModelo) {
        super(rs, pModelo);
    }

    public Usuario(ModeloOrm pModelo) {
        super(pModelo);
    }
	
	@ColumnaAnotaciones(campo = "id_usuario", tipo = TEXTO, esId = true, esLlaveAutoGenerada = true)
	private String idUsuario;
	
	@ColumnaAnotaciones(campo = "nombres", tipo = TEXTO)
	private String nombres;
	
	@ColumnaAnotaciones(campo = "apellidos", tipo = TEXTO)
	private String apellidos;
	
	@ColumnaAnotaciones(campo = "tipo_doc", tipo = TEXTO)
	private String tipoDoc;
	
	@ColumnaAnotaciones(campo = "documento", tipo = TEXTO)
	private String documento;
	
	@ColumnaAnotaciones(campo = "nick", tipo = TEXTO)
	private String nick;
	
	@ColumnaAnotaciones(campo = "contrasena", tipo = TEXTO)
	private String contrasena;
	
	@ColumnaAnotaciones(campo = "estado", tipo = TEXTO)
	private String estado;
	
	@ColumnaAnotaciones(campo = "id_rol_fk", tipo = TEXTO)
	private String idRolFk;
		
	@ColumnaAnotaciones(campo = "fecha_creacion", tipo = TEXTO)
	private String fechaCreacion;

	public String getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getIdRolFk() {
		return idRolFk;
	}

	public void setIdRolFk(String idRolFk) {
		this.idRolFk = idRolFk;
	}


	public String getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getTipoDoc() {
		return tipoDoc;
	}

	public void setTipoDoc(String tipoDoc) {
		this.tipoDoc = tipoDoc;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String generarContrasena(String pPassword) {
		String nuevaContrasena;
		try {
			nuevaContrasena = EncriptadorAES.encriptar(pPassword, getFechaCreacion());
		} catch (Exception e) {
			nuevaContrasena = "123456";
		}
		setContrasena(nuevaContrasena);
		
		return nuevaContrasena;
	}
	
	/*public static String generarContrasena(ModeloOrm modelo, int idUsuario, String pPassword) {
		String sql = "SELECT codigo_usuario, id_unico_usuario, fecha_Registro";
		sql += " FROM tbl_usuario WHERE `codigo_usuario` = '" + idUsuario + "'";
		
		JSONArray consulta = modelo.executeQuery(sql);
		if(consulta.length() > 0) {
			try {
				JSONObject temp = consulta.getJSONObject(0);
				String codigoUsuario = CoviLibUtils.getDataJSon(temp, "codigo_usuario"); 
				String idUnico = CoviLibUtils.getDataJSon(temp, "id_unico_usuario");
				
				return EncriptadorAES.encriptar(pPassword, codigoUsuario + idUnico);
				
			} catch (Exception e) {
			}
			
		}
		return null;
		
	}
	
	public static boolean actualizarContrasena(ModeloOrm modelo, int idUsuario, String pPassword, int pIdCentro) throws Exception {
		String newPass = Usuario.generarContrasena(modelo, idUsuario, pPassword);
		
		String sql = "update " + ObjectOrm.obtenerNombreTabla(Usuario.class) + " set ";
		sql += " `clavesegura` = '" + newPass + "' ";
		sql +=  " where codigo_usuario = " + idUsuario; 
		
		return  modelo.actualizarTodo(sql);		
		
	}
	
	public static boolean actualizarEstado(ModeloOrm modelo, int idUsuario, String pEstado, int pIdCentro) throws Exception {
		
		String sql = "update " + ObjectOrm.obtenerNombreTabla(Usuario.class) + " set ";
		sql += " estado = '" + pEstado + "' ";
		sql +=  " where codigo_usuario = " + idUsuario + " AND id_centro = " + pIdCentro; 
		
		return  modelo.actualizarTodo(sql);		
		
	}*/
	
}
