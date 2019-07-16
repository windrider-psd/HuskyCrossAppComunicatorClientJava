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
        
        Map<String, String> commandArgs = new LinkedHashMap<>();
        commandArgs.put("user", "51a");
        commandArgs.put("number", "10");
        //commandArgs.put("champ", "ULTRA GIGA POGCHAMP");
                
        Command command = new Command();
        command.setCommandId(1);
        command.setCommandPath("pog");
        command.setOperationType(OperationType.READ);
        command.setArgs(commandArgs);
        
        CACClient cACClient = new CACClient("br.ufsm.husky.cac.test", null, 0);
       
        cACClient.InvokeRoutes(command);
        
       
    }
}
