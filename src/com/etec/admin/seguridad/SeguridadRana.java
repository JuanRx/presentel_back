package com.etec.admin.seguridad;

import com.covilib.security.EncriptadorAES;

public class SeguridadRana {
	
	public static String sacarMacDeIdUnico(String idu){
		
		String res = "";
		int pos = 7;
		res += idu.substring(pos,pos + 2); 
		for(int i=0;i<5;i++){
			pos+=5;
			res += "-"+idu.substring(pos,pos+2);
		}
		return res;
	}

	public static String sacarfechaHoraDeIdUnico(String idu){
		
		String ped = idu.substring(40,44);
		
		ped += "-"+idu.substring(47,49);
		
		ped += "-"+idu.substring(55,57);
		
		ped += " "+idu.substring(60,62);
		
		ped += ":"+idu.substring(65,67);
		
		ped += ":"+idu.substring(70,72);
		
		return ped;

	}
	
	public static String sacarSemilla(String idu) throws Exception{
		try{
			String res = "";
			String tmp1 = sacarMacDeIdUnico(idu) + sacarfechaHoraDeIdUnico(idu);
		
			res = tmp1.substring(16,17);
			res += ""+tmp1.substring(15,16);
			res += ""+tmp1.substring(35,36);
			res += ""+tmp1.substring(34,35);
			res += ""+tmp1.substring(7,8);
			res += ""+tmp1.substring(6,7);
			res += ""+tmp1.substring(28,29);
			res += ""+tmp1.substring(35,36);
		
			return res;
		}catch(Exception er){
		  throw new Exception("No existen llaves");
	  }
	}
	
	public static void main(String args[]) {
		/*
		d7063064ee93b2a9
		9f9074679aaaa9a0
		55ff362e6c9a2a13
		 */
		String idunicoRana = "ayd7063064ee93b2a9";
		
		if(idunicoRana.indexOf("ay") == 0) {
			idunicoRana = idunicoRana.substring(2);
		}
		
		System.out.println(generaClaveAndroid(idunicoRana));
	}
	
	public static String generaClaveAndroid(String pLlave) {
		//EncriptadorAES encriptador = new EncriptadorAES();
        
		String semilla = generaSemillaAndroid(pLlave);
		
        String encriptado;
		try {
			encriptado = EncriptadorAES.encriptar(pLlave, semilla);
		} catch (Exception e) {
			return null;
		}
        
        return resumirClave(encriptado, 12);
	}
	
	public static String resumirClave(String clave, int numeroCaracteres) {
		String respuesta = "";
		
		for(int i = 0; i < clave.length(); i++) {
			char ch = clave.charAt(i); 
			if( (Character.isDigit(ch)) || (Character.isLetter(ch)) ) {
				respuesta += ch;
			}
			if( respuesta.length() >= numeroCaracteres ) {
				break;
			}
		}
		
		return respuesta.toLowerCase();
	}
	
	public static String generaSemillaAndroid(String pLlave) {
		for(int i = 0; i < pLlave.length(); i++) {
			if(Character.isDigit(pLlave.charAt(i))) {
				return generaSemillaAndroid(Integer.parseInt("" + pLlave.charAt(i)));
			}
		}
		return generaSemillaAndroid(10);
	}
	
	public static String generaSemillaAndroid(int idSemilla) {
		switch (idSemilla) {
		case 0:
			return "f8952a5c-46ec-471c-856f-eee2c41e5022";
		case 1:
			return "ebdc41b5-6658-43b8-b448-14e7024ec8a5";
		case 2:
			return "6cfa5ec4-7274-45bb-a845-d09fec692e97";
		case 3:
			return "5bf18175-1aeb-4c45-8d23-697a8cb25616";
		case 4:
			return "7ab176fd-bcbf-41d2-bd6a-d6efd8b3344a";
		case 5:
			return "317ad553-a1ba-4023-8984-ceb0c066ba00";
		case 6:
			return "4db0258e-6369-41bc-80d2-ee8f211c4010";
		case 7:
			return "bc50d454-14d7-48e8-8179-09a3c4d26299";
		case 8:
			return "8bab0eb7-991c-4c62-a5ac-32d5dff865b3";
		case 9:
			return "48d9bbab-c2a3-438d-8c4d-367596686540";			
		default:
			return "90884fb2-e85e-4d17-8a26-4e2923e87ce6";
		}
	}
	
}
