package entities;

import static com.covilib.orm.ModeloOrm.TipoDatoORM.TEXTO;

import org.json.JSONObject;

import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
import com.covilib.orm.annotations.ColumnaAnotaciones;
import com.covilib.orm.annotations.TablaAnotaciones;

@TablaAnotaciones(nombreTabla = "tbl_ficha", prefijoId = "FICHA")
public class Ficha extends ObjectOrm{
	
	public Ficha(JSONObject rs, ModeloOrm pModelo) {
        super(rs, pModelo);
    }

    public Ficha(ModeloOrm pModelo) {
        super(pModelo);
    }
    
    @ColumnaAnotaciones(campo = "id_ficha", tipo = TEXTO, esId = true, esLlaveAutoGenerada = true)
	private String idFicha;
    
    @ColumnaAnotaciones(campo = "ficha", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String ficha;
    
    @ColumnaAnotaciones(campo = "programa", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String programa;
    
    @ColumnaAnotaciones(campo = "fecha_inicio", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String fechaInicio;
    
    @ColumnaAnotaciones(campo = "fecha_fin", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String fechaFin;

    public String getIdFicha() {
		return idFicha;
	}

	public void setIdFicha(String idFicha) {
		this.idFicha = idFicha;
	}

	public String getFicha() {
		return ficha;
	}

	public void setFicha(String ficha) {
		this.ficha = ficha;
	}

	public String getPrograma() {
		return programa;
	}

	public void setPrograma(String programa) {
		this.programa = programa;
	}

	public String getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public String getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}  
	
	
    
}