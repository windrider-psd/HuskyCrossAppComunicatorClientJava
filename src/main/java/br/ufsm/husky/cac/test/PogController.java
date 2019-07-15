/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.husky.cac.test;

import br.ufsm.husky.cac.Listener;
import br.ufsm.husky.cac.ListenerRoute;
import br.ufsm.husky.cac.OperationType;

/**
 *
 * @author politecnico
 */
@Listener
public class PogController
{
    
    @ListenerRoute(CommandPath = "pog", OperationType = OperationType.READ)
    public void POGGERS(UserData userdata)
    {
        System.out.println("REGULAR POGGERS");
        System.out.println(userdata.user);
        System.out.println(userdata.password);
    }
    @ListenerRoute(CommandPath = "pog", OperationType = OperationType.READ)
    public void USERDATAPOGGERS(String user, String password)
    {
        System.out.println("USERDATA POGGERS");
        System.out.println(user);
        System.out.println(password);
    }
    
    @ListenerRoute(CommandPath = "pog", OperationType = OperationType.READ)
    public void CRAZYPOGGERS(String userdata)
    {
        System.out.println("CRAZY POGGERS");
        System.out.println(userdata);
    }
    
    @ListenerRoute(CommandPath = "pog", OperationType = OperationType.READ)
    public void MATHPOGGERS(int userdata)
    {
        System.out.println("MATH POGGERS");
        System.out.println(userdata);
    }
    
    @ListenerRoute(CommandPath = "pog", OperationType = OperationType.READ)
    public void ULTRAPOGGERS(String pog, String champ)
    {
        System.out.println("ULTRA POGGERS");
        System.out.println(pog);
        System.out.println(champ);
    }
    
}
