/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.husky.cac;

import java.util.Map;

/**
 *
 * @author politecnico
 */
public class Command
{
    private int commandId;
    private String commandPath;
    private OperationType operationType;
    private Map<String, String> args;
    
    public Command()
    {
    }

    public Command(int commandId, String commandPath, OperationType operationType, Map<String, String> args)
    {
        this.commandId = commandId;
        this.commandPath = commandPath;
        this.operationType = operationType;
        this.args = args;
    }

    public Map<String, String> getArgs()
    {
        return args;
    }

    public void setArgs(Map<String, String> args)
    {
        this.args = args;
    }

    public int getCommandId()
    {
        return commandId;
    }

    public void setCommandId(int commandId)
    {
        this.commandId = commandId;
    }

   

    public String getCommandPath()
    {
        return commandPath;
    }

    public void setCommandPath(String commandPath)
    {
        this.commandPath = commandPath;
    }

    public OperationType getOperationType()
    {
        return operationType;
    }

    public void setOperationType(OperationType operationType)
    {
        this.operationType = operationType;
    }


    
    
}
