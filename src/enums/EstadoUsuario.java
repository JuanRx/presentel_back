package enums;

public enum EstadoUsuario {
    
	HABILITADO, INHABILITADO;
	
	public String toString(){
		String temp = "" + super.toString();
		return "" + temp.charAt(0);
	}
	
	public static boolean stringValid(String str){
        try{
        	EstadoUsuario.valueOf(str);
            return true;
        }catch(Exception err){
            return false;
        }
    }
	
	public static String[] toArrayString(){
		String[] result = new String[EstadoUsuario.values().length]; 
		for ( int i = 0; i < EstadoUsuario.values().length ; i++) { 
			EstadoUsuario estado = EstadoUsuario.values()[i];
			result[i] = "" + estado;
		}
		return result;
	}
    
}
