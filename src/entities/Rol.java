package entities;

import static com.covilib.orm.ModeloOrm.TipoDatoORM.TEXTO;

import org.json.JSONObject;

import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
import com.covilib.orm.annotations.ColumnaAnotaciones;
import com.covilib.orm.annotations.TablaAnotaciones;

@TablaAnotaciones(nombreTabla = "tbl_rol", prefijoId = "ROL")
public class Rol extends ObjectOrm{
	
	public Rol(JSONObject rs, ModeloOrm pModelo) {
        super(rs, pModelo);
    }

    public Rol(ModeloOrm pModelo) {
        super(pModelo);
    }
    
    @ColumnaAnotaciones(campo = "id_rol", tipo = TEXTO, esId = true, esLlaveAutoGenerada = true)
	private String idRol;
    
    @ColumnaAnotaciones(campo = "rol", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String rol;

	public String getIdRol() {
		return idRol;
	}

	public void setIdRol(String idRol) {
		this.idRol = idRol;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}
    
   
    
}
