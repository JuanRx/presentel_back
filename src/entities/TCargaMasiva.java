package entities;

import static com.covilib.orm.ModeloOrm.TipoDatoORM.TEXTO;

import org.json.JSONObject;

import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
import com.covilib.orm.annotations.ColumnaAnotaciones;
import com.covilib.orm.annotations.TablaAnotaciones;

@TablaAnotaciones(nombreTabla = "tbl_tempCM", prefijoId = "TCM")
public class TCargaMasiva extends ObjectOrm{
	
	public TCargaMasiva(JSONObject rs, ModeloOrm pModelo) {
        super(rs, pModelo);
    }

    public TCargaMasiva(ModeloOrm pModelo) {
        super(pModelo);
    }
    
    @ColumnaAnotaciones(campo = "id_carga", tipo = TEXTO, esId = true, esLlaveAutoGenerada = true)
	private String idCarga;
    
    @ColumnaAnotaciones(campo = "fecha_carga", tipo = TEXTO)
	private String fechaCarga;
    
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

	public String getIdCarga() {
		return idCarga;
	}

	public void setIdCarga(String idCarga) {
		this.idCarga = idCarga;
	}

	public String getFechaCarga() {
		return fechaCarga;
	}

	public void setFechaCarga(String fechaCarga) {
		this.fechaCarga = fechaCarga;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
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
	
	
    
   
    
}
