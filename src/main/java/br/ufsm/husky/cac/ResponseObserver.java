/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.husky.cac;

/**
 *
 * @author politecnico
 */
public interface CACClientCommandObserver
{
    public void OnCommand(Command nodeJSCommand);
}
