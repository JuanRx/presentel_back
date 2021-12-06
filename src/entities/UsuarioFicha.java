package entities;

import static com.covilib.orm.ModeloOrm.TipoDatoORM.TEXTO;

import org.json.JSONObject;

import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
import com.covilib.orm.annotations.ColumnaAnotaciones;
import com.covilib.orm.annotations.TablaAnotaciones;

@TablaAnotaciones(nombreTabla = "tbl_usuario_ficha", prefijoId = "USFC")
public class UsuarioFicha extends ObjectOrm{
	
	public UsuarioFicha(JSONObject rs, ModeloOrm pModelo) {
        super(rs, pModelo);
    }

    public UsuarioFicha(ModeloOrm pModelo) {
        super(pModelo);
    }
    
    @ColumnaAnotaciones(campo = "id_usuario_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idUsuario;
    
    @ColumnaAnotaciones(campo = "id_ficha_fk", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idFicha;

	public String getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getIdFicha() {
		return idFicha;
	}

	public void setIdFicha(String idFicha) {
		this.idFicha = idFicha;
	}
     
}
