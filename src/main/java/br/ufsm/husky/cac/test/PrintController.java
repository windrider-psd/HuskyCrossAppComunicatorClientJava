/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufsm.husky.cac.test;

import br.ufsm.husky.cac.Argument;
import br.ufsm.husky.cac.Listener;
import br.ufsm.husky.cac.ListenerRoute;
import br.ufsm.husky.cac.OperationType;

/**
 *
 * @author aluno
 */

@Listener
public class PrintController {
    
    
    @ListenerRoute(CommandPath = "printa", OperationType = OperationType.WRITE)
    public String nome(@Argument(Name = "usuario") Usuario usuario)
    {
        System.out.println(usuario.getNome());
        System.out.println(usuario.getIdade());
        return "hahahaha object";
    }
    
    @ListenerRoute(CommandPath = "printa", OperationType = OperationType.WRITE)
    public String nomeString(@Argument(Name = "name") String name)
    {
        System.out.println(name);
        return "hahahaha string";
    }
}
