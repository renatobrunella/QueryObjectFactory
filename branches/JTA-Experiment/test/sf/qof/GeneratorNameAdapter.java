package sf.qof;

import static sf.qof.codegen.Constants.SIG_getString;
import static sf.qof.codegen.Constants.SIG_getStringNamed;
import static sf.qof.codegen.Constants.SIG_registerOutParameter;
import static sf.qof.codegen.Constants.SIG_setNull;
import static sf.qof.codegen.Constants.SIG_setString;
import static sf.qof.codegen.Constants.TYPE_CallableStatement;
import static sf.qof.codegen.Constants.TYPE_PreparedStatement;
import static sf.qof.codegen.Constants.TYPE_ResultSet;
import static sf.qof.codegen.Constants.TYPE_int;

import java.util.HashSet;
import java.util.Set;

import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import sf.qof.adapter.GeneratorMappingAdapter;
import sf.qof.mapping.ParameterMapping;
import sf.qof.mapping.ResultMapping;


public class GeneratorNameAdapter implements GeneratorMappingAdapter {

  public Set<Class<?>> getTypes() {
    Set<Class<?>> types = new HashSet<Class<?>>();
    types.add(Name.class);
    return types;
  }

  final static Type TYPE_Name = Type.getType(Name.class);
  final static Signature SIG_Name_Constructor = new Signature("<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
  final static Signature SIG_getFirstName = new Signature("getFirstName", "()Ljava/lang/String;");
  final static Signature SIG_getLastName = new Signature("getLastName", "()Ljava/lang/String;");

  public void generateFromResult(ResultMapping resultMapping, CodeEmitter co, Local result, int[] indexes) {
    co.new_instance(TYPE_Name);
    co.dup();
    co.load_local(result);
    co.push(indexes[0]);
    co.invoke_interface(result.getType(), SIG_getString);
    co.load_local(result);
    co.push(indexes[1]);
    co.invoke_interface(result.getType(), SIG_getString);
    co.invoke_constructor(TYPE_Name, SIG_Name_Constructor);
  }

  public void generateFromResultSet(ResultMapping resultMapping, CodeEmitter co, Local resultSet, String[] columns) {
    co.new_instance(TYPE_Name);
    co.dup();
    co.load_local(resultSet);
    co.push(columns[0]);
    co.invoke_interface(TYPE_ResultSet, SIG_getStringNamed);
    co.load_local(resultSet);
    co.push(columns[1]);
    co.invoke_interface(TYPE_ResultSet, SIG_getStringNamed);
    co.invoke_constructor(TYPE_Name, SIG_Name_Constructor);
  }

  public void generateToPreparedStatement(ParameterMapping parameterMapping, CodeEmitter co, Local preparedStatement, int[] indexes, Local indexOffset) {
    // value is on the stack
    Label labelNull = co.make_label();
    Label labelEnd = co.make_label();
    Local value = co.make_local(TYPE_Name);
    co.checkcast(TYPE_Name);
    co.dup();
    co.store_local(value);
    co.ifnull(labelNull);

    co.load_local(preparedStatement);
    co.dup();
    co.push(indexes[0]);
    if (indexOffset != null) {
      co.load_local(indexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
    }
    co.load_local(value);
    co.invoke_virtual(TYPE_Name, SIG_getFirstName);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setString);

    co.push(indexes[1]);
    if (indexOffset != null) {
      co.load_local(indexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
    }
    co.load_local(value);
    co.invoke_virtual(TYPE_Name, SIG_getLastName);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setString);

    co.goTo(labelEnd);

    co.mark(labelNull);
    co.load_local(preparedStatement);
    co.dup();
    co.push(indexes[0]);
    if (indexOffset != null) {
      co.load_local(indexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
    }
    co.push(java.sql.Types.VARCHAR);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setNull);

    co.push(indexes[1]);
    if (indexOffset != null) {
      co.load_local(indexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
    }
    co.push(java.sql.Types.VARCHAR);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setNull);

    co.mark(labelEnd);
  }

  public void generateRegisterOutputParameters(ResultMapping resultMapping, CodeEmitter co, Local callableStatement,
      int[] indexes) {
    co.load_local(callableStatement);
    co.push(indexes[0]);
    co.push(java.sql.Types.VARCHAR);
    co.invoke_interface(TYPE_CallableStatement, SIG_registerOutParameter);
    co.load_local(callableStatement);
    co.push(indexes[1]);
    co.push(java.sql.Types.VARCHAR);
    co.invoke_interface(TYPE_CallableStatement, SIG_registerOutParameter);
  }

  public int getNumberOfColumns() {
    return 2;
  }

  private final static int[] types = new int[] { java.sql.Types.VARCHAR, java.sql.Types.VARCHAR };

  public int[] preferredSqlTypes() {
    return types;
  }
}
