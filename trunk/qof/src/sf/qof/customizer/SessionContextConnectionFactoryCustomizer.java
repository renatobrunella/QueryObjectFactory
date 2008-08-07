/*
 * Copyright 2007 brunella ltd
 *
 * Licensed under the LGPL Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package sf.qof.customizer;

import static sf.qof.codegen.Constants.SIG_getConnection;
import static sf.qof.codegen.Constants.SIG_setConnection;
import static sf.qof.codegen.Constants.TYPE_RuntimeException;
import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.Signature;

import org.objectweb.asm.Type;

import sf.qof.session.UseSessionContext;

/**
 * Provides the implementation of a ConnectionFactoryCustomizer to use the
 * SessionContextFactory.
 * 
 * This customizer implements <code>getConnection()</code> and
 * <code>setConnection()</code> methods. It delegates calls to
 * <code>getConnection()</code> to
 * <code>SessionContextFactory.getContext().getConnection()</code>. Calls to
 * <code>setConnection()</code> throws a RuntimeException.
 * 
 * The <code>UseSessionContext</code> annotation indicates that this
 * customizer should be used and allows to specify a session context name.
 * 
 * @see sf.qof.session.SessionContextFactory
 * @see sf.qof.session.UseSessionContext
 */
public class SessionContextConnectionFactoryCustomizer implements ConnectionFactoryCustomizer {

  private static final Type TYPE_SessionContextFactory = Type.getType("Lsf/qof/session/SessionContextFactory;");
  private static final Type TYPE_SessionContext = Type.getType("Lsf/qof/session/SessionContext;");
  private static final Signature SIG_getContext = new Signature("getContext", "()Lsf/qof/session/SessionContext;");
  private static final Signature SIG_getContextWithName = new Signature("getContext", "(Ljava/lang/String;)Lsf/qof/session/SessionContext;");

  public void emitFields(Class<?> queryDefinitionClass, Class<?> superClass, ClassEmitter ce) {
    // no fields needed
  }

  public void emitGetConnection(Class<?> queryDefinitionClass, Class<?> superClass, ClassEmitter ce) {
    CodeEmitter co = ce.begin_method(Constants.ACC_PUBLIC, SIG_getConnection, null, null);
    if (queryDefinitionClass.isAnnotationPresent(UseSessionContext.class)) {
      UseSessionContext sessionContextAnnotation = queryDefinitionClass.getAnnotation(UseSessionContext.class);
      co.push(sessionContextAnnotation.name());
      co.invoke_static(TYPE_SessionContextFactory, SIG_getContextWithName);
    } else {
      co.invoke_static(TYPE_SessionContextFactory, SIG_getContext);
    }
    co.invoke_interface(TYPE_SessionContext, SIG_getConnection);
    co.return_value();
    co.end_method();
  }

  public void emitSetConnection(Class<?> queryDefinitionClass, Class<?> superClass, ClassEmitter ce) {
    CodeEmitter co = ce.begin_method(Constants.ACC_PUBLIC, SIG_setConnection, null, null);
    co.throw_exception(TYPE_RuntimeException, "Connection cannot be set");
    co.end_method();
  }

}