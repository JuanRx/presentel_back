package enums;

public enum TipoActivacion {
	
	RANA_PI, RANA_ANDROID;
	
	public String toString(){
		String temp = "" + super.toString();
		return "" + temp.charAt(0);
	}
	
	public static boolean stringValid(String str){
        try{
        	TipoActivacion.valueOf(str);
            return true;
        }catch(Exception err){
            return false;
        }
    }
	
	public static String[] toArrayString(){
		String[] result = new String[TipoActivacion.values().length]; 
		for ( int i = 0; i < TipoActivacion.values().length ; i++) { 
			TipoActivacion item = TipoActivacion.values()[i];
			result[i] = "" + item;
		}
		return result;
	}
	
}
