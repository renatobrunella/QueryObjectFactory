/*
 * Copyright 2010 brunella ltd
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
package sf.qof.bundle;

import org.osgi.framework.BundleContext;

/**
 * Internal - logging service factory.
 * 
 * @since 1.1.0
 */
public class LoggerFactory {

  /**
   * Return a <code>Logger</code> that uses the OSGi LogService if it is available
   * or a null-logger if not.
   * 
   * @param   context the bundle context
   * @return  a <code>Logger</code> implementation
   * 
   * @since 1.1.0
   */
  public static Logger getLogger(BundleContext context) {
    try {
      LoggerFactory.class.getClassLoader().loadClass("org.osgi.service.log.LogService");
      return new LoggerImpl(context);
    } catch (ClassNotFoundException e) {
      return new NullLogger();
    }
  }
}
