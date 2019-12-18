package br.ufsm.husky.cac.test;


import br.ufsm.husky.cac.CACClient;
import br.ufsm.husky.cac.Command;
import br.ufsm.husky.cac.OperationType;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author politecnico
 */
public class Main
{
    public static void main(String[] args) throws InstantiationException, IllegalAccessException
    {
        
        Usuario u = new Usuario();
        u.setIdade(21);
        u.setNome("Christian");
        Map<String, Object> commandArgs = new LinkedHashMap<>();
        commandArgs.put("usuario", u);
        commandArgs.put("gato", "chunchunmaru");
      
               
        
        CACClient client = new CACClient("br.ufsm.husky.cac.test", "localhost", 961);
        
        client.WriteCommand("pog", OperationType.READ, commandArgs);
       
    }
}
