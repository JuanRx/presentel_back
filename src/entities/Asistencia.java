package entities;

import static com.covilib.orm.ModeloOrm.TipoDatoORM.TEXTO;

import org.json.JSONObject;

import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
import com.covilib.orm.annotations.ColumnaAnotaciones;
import com.covilib.orm.annotations.TablaAnotaciones;

@TablaAnotaciones(nombreTabla = "tbl_asistencia", prefijoId = "ASISTENCIA")
public class Asistencia extends ObjectOrm{
	
	public Asistencia(JSONObject rs, ModeloOrm pModelo) {
        super(rs, pModelo);
    }

    public Asistencia(ModeloOrm pModelo) {
        super(pModelo);
    }
    
    @ColumnaAnotaciones(campo = "id_asistencia", tipo = TEXTO, esId = true, esLlaveAutoGenerada = true)
	private String idAsistencia;
    
    @ColumnaAnotaciones(campo = "fecha_asistencia", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String fechaAsistencia;

    @ColumnaAnotaciones(campo = "toma_asistencia", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String tomaAsistencia;
    
    @ColumnaAnotaciones(campo = "id_aprendiz_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idAprendizFk;
    
    @ColumnaAnotaciones(campo = "id_instructor_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idInstructorFk;
    
    @ColumnaAnotaciones(campo = "id_tematica_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idTematicaFk;
    
    @ColumnaAnotaciones(campo = "id_ficha_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idFichaFk;

	public String getIdAsistencia() {
		return idAsistencia;
	}

	public void setIdAsistencia(String idAsistencia) {
		this.idAsistencia = idAsistencia;
	}

	public String getFechaAsistencia() {
		return fechaAsistencia;
	}

	public void setFechaAsistencia(String fechaAsistencia) {
		this.fechaAsistencia = fechaAsistencia;
	}

	public String getTomaAsistencia() {
		return tomaAsistencia;
	}

	public void setTomaAsistencia(String tomaAsistencia) {
		this.tomaAsistencia = tomaAsistencia;
	}

	public String getIdAprendizFk() {
		return idAprendizFk;
	}

	public void setIdAprendizFk(String idAprendizFk) {
		this.idAprendizFk = idAprendizFk;
	}

	public String getIdInstructorFk() {
		return idInstructorFk;
	}

	public void setIdInstructorFk(String idInstructorFk) {
		this.idInstructorFk = idInstructorFk;
	}

	public String getIdTematicaFk() {
		return idTematicaFk;
	}

	public void setIdTematicaFk(String idTematicaFk) {
		this.idTematicaFk = idTematicaFk;
	}

	public String getIdFichaFk() {
		return idFichaFk;
	}

	public void setIdFichaFk(String idFichaFk) {
		this.idFichaFk = idFichaFk;
	}
    
    
   
    
}
