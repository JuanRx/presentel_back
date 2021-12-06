package entities;

import static com.covilib.orm.ModeloOrm.TipoDatoORM.TEXTO;

import org.json.JSONObject;

import com.covilib.orm.ModeloOrm;
import com.covilib.orm.ObjectOrm;
import com.covilib.orm.annotations.ColumnaAnotaciones;
import com.covilib.orm.annotations.TablaAnotaciones;

@TablaAnotaciones(nombreTabla = "tbl_notificaciones", prefijoId = "ROL")
public class Notificaciones extends ObjectOrm{
	
	public Notificaciones(JSONObject rs, ModeloOrm pModelo) {
        super(rs, pModelo);
    }

    public Notificaciones(ModeloOrm pModelo) {
        super(pModelo);
    }
    
    @ColumnaAnotaciones(campo = "id_notificacion", tipo = TEXTO, esId = true, esLlaveAutoGenerada = true)
	private String idNotificacion;
    
    @ColumnaAnotaciones(campo = "fecha_notificacion", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String fechaNotifica;

    @ColumnaAnotaciones(campo = "id_usuario_notifica", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idUsuarioNoti;
    
    @ColumnaAnotaciones(campo = "id_usuario_notificado", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String idUsuarioNotif;
    
    @ColumnaAnotaciones(campo = "motivo", tipo = TEXTO, esId = true/*, esLlaveAutoGenerada = true*/)
	private String motivo;

	public String getIdNotificacion() {
		return idNotificacion;
	}

	public void setIdNotificacion(String idNotificacion) {
		this.idNotificacion = idNotificacion;
	}

	public String getFechaNotifica() {
		return fechaNotifica;
	}

	public void setFechaNotifica(String fechaNotifica) {
		this.fechaNotifica = fechaNotifica;
	}

	public String getIdUsuarioNoti() {
		return idUsuarioNoti;
	}

	public void setIdUsuarioNoti(String idUsuarioNoti) {
		this.idUsuarioNoti = idUsuarioNoti;
	}

	public String getIdUsuarioNotif() {
		return idUsuarioNotif;
	}

	public void setIdUsuarioNotif(String idUsuarioNotif) {
		this.idUsuarioNotif = idUsuarioNotif;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
    
   
    
}
