package entities;

import static com.covilib.orm.ModeloOrm.TipoDatoORM.TEXTO;

import org.json.JSONObject;

import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
import com.covilib.orm.annotations.ColumnaAnotaciones;
import com.covilib.orm.annotations.TablaAnotaciones;

@TablaAnotaciones(nombreTabla = "tbl_tematica", prefijoId = "TEMA")
public class Tematica extends ObjectOrm{
	
	public Tematica(JSONObject rs, ModeloOrm pModelo) {
        super(rs, pModelo);
    }

    public Tematica(ModeloOrm pModelo) {
        super(pModelo);
    }
    
    @ColumnaAnotaciones(campo = "id_tematica", tipo = TEXTO, esId = true, esLlaveAutoGenerada = true)
	private String idTematica;
    
    @ColumnaAnotaciones(campo = "nombre_tematica", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String nombreTematica;

    @ColumnaAnotaciones(campo = "id_instructor_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idInstructorFk;
    
    @ColumnaAnotaciones(campo = "id_ficha_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idFichaFk;

	public String getIdTematica() {
		return idTematica;
	}

	public void setIdTematica(String idTematica) {
		this.idTematica = idTematica;
	}

	public String getNombreTematica() {
		return nombreTematica;
	}

	public void setNombreTematica(String nombreTematica) {
		this.nombreTematica = nombreTematica;
	}

	public String getIdInstructorFk() {
		return idInstructorFk;
	}

	public void setIdInstructorFk(String idInstructorFk) {
		this.idInstructorFk = idInstructorFk;
	}

	public String getIdFichaFk() {
		return idFichaFk;
	}

	public void setIdFichaFk(String idFichaFk) {
		this.idFichaFk = idFichaFk;
	}
        
}
