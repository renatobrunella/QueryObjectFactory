package sf.qof.codegen;

public interface Enhancer {

  public abstract <T> Class<T> enhance(Class<T> queryDefinitionClass, Class<T> superClass);

}