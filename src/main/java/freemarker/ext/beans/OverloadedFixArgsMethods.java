/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */
package freemarker.ext.beans;

import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Stores the non-varargs methods for a {@link OverloadedMethods} object.
 * @author Attila Szegedi
 */
class OverloadedFixArgsMethods extends OverloadedMethodsSubset
{
    void beforeWideningUnwrappingHints(Member member, Class[] paramTypes) {
        // Do nothing
    }
    
    void afterWideningUnwrappingHints(int paramCount) {
        // Do nothing
    }

    Object getMemberAndArguments(List tmArgs, BeansWrapper w) 
    throws TemplateModelException {
        if(tmArgs == null) {
            // null is treated as empty args
            tmArgs = Collections.EMPTY_LIST;
        }
        final int argCount = tmArgs.size();
        final Class[][] unwrappingHintsByParamCount = getUnwrappingHintsByParamCount();
        if(unwrappingHintsByParamCount.length <= argCount) {
            return NO_SUCH_METHOD;
        }
        Class[] unwarppingArgumentTypes = unwrappingHintsByParamCount[argCount];
        if(unwarppingArgumentTypes == null) {
            return NO_SUCH_METHOD;
        }
        //assert types.length == l;
        // Marshal the arguments
        Object[] pojoArgs = new Object[argCount];
        Iterator it = tmArgs.iterator();
        for(int i = 0; i < argCount; ++i) {
            Object pojo = w.unwrapInternal((TemplateModel)it.next(), unwarppingArgumentTypes[i]);
            if(pojo == BeansWrapper.CAN_NOT_UNWRAP) {
                return NO_SUCH_METHOD;
            }
            pojoArgs[i] = pojo;
        }
        
        Object objMember = getMemberForArgs(pojoArgs, false);
        if(objMember instanceof Member) {
            Member member = (Member) objMember;
            BeansWrapper.coerceBigDecimals(getSignature(member), pojoArgs);
            return new MemberAndArguments(member, pojoArgs);
        } else {
            return objMember; // either NO_SUCH_METHOD or AMBIGUOUS_METHOD
        }
    }
}
