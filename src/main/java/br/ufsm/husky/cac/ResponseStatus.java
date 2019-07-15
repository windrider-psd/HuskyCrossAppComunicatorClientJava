/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.husky.cac;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @author politecnico
 */
public enum ResponseStatus
{
    OK, INVALID, ERROR;
    
    @JsonValue
    public int toValue() {
        return ordinal();
    }
}
