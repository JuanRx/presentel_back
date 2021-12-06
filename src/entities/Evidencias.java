package entities;

import static com.covilib.orm.ModeloOrm.TipoDatoORM.TEXTO;

import org.json.JSONObject;

import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
import com.covilib.orm.annotations.ColumnaAnotaciones;
import com.covilib.orm.annotations.TablaAnotaciones;

@TablaAnotaciones(nombreTabla = "tbl_rol", prefijoId = "ROL")
public class Evidencias extends ObjectOrm{
	
	public Evidencias(JSONObject rs, ModeloOrm pModelo) {
        super(rs, pModelo);
    }

    public Evidencias(ModeloOrm pModelo) {
        super(pModelo);
    }
    
    @ColumnaAnotaciones(campo = "id_evidencia", tipo = TEXTO, esId = true, esLlaveAutoGenerada = true)
	private String idEvidencia;
    
    @ColumnaAnotaciones(campo = "evidencia", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String evidencia;
    
    @ColumnaAnotaciones(campo = "fecha_envio", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String fechaEnvio;
    
    @ColumnaAnotaciones(campo = "fecha_validacion", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String fechaValidacion;
    
    @ColumnaAnotaciones(campo = "estado_validacion", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String estadoValidacion;
    
    @ColumnaAnotaciones(campo = "id_usuario_envia", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idUsuarioEnvia;
    
    @ColumnaAnotaciones(campo = "id_ficha_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idFichaFk;
    
    @ColumnaAnotaciones(campo = "id_tematica_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idTematicaFk;
    
    @ColumnaAnotaciones(campo = "id_instructor_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idInstructorFk;

	public String getIdEvidencia() {
		return idEvidencia;
	}

	public void setIdEvidencia(String idEvidencia) {
		this.idEvidencia = idEvidencia;
	}

	public String getEvidencia() {
		return evidencia;
	}

	public void setEvidencia(String evidencia) {
		this.evidencia = evidencia;
	}

	public String getFechaEnvio() {
		return fechaEnvio;
	}

	public void setFechaEnvio(String fechaEnvio) {
		this.fechaEnvio = fechaEnvio;
	}

	public String getFechaValidacion() {
		return fechaValidacion;
	}

	public void setFechaValidacion(String fechaValidacion) {
		this.fechaValidacion = fechaValidacion;
	}

	public String getEstadoValidacion() {
		return estadoValidacion;
	}

	public void setEstadoValidacion(String estadoValidacion) {
		this.estadoValidacion = estadoValidacion;
	}

	public String getIdUsuarioEnvia() {
		return idUsuarioEnvia;
	}

	public void setIdUsuarioEnvia(String idUsuarioEnvia) {
		this.idUsuarioEnvia = idUsuarioEnvia;
	}

	public String getIdFichaFk() {
		return idFichaFk;
	}

	public void setIdFichaFk(String idFichaFk) {
		this.idFichaFk = idFichaFk;
	}

	public String getIdTematicaFk() {
		return idTematicaFk;
	}

	public void setIdTematicaFk(String idTematicaFk) {
		this.idTematicaFk = idTematicaFk;
	}

	public String getIdInstructorFk() {
		return idInstructorFk;
	}

	public void setIdInstructorFk(String idInstructorFk) {
		this.idInstructorFk = idInstructorFk;
	}
	
	
    
}
