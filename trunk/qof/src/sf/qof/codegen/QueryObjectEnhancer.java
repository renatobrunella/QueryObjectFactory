package sf.qof.codegen;

public class QueryObjectEnhancer implements Enhancer {

  public <T> Class<T> enhance(Class<T> queryDefinitionClass, Class<T> superClass) {
    // TODO enhancer registry
    return new SessionRunnerEnhancer().enhance(queryDefinitionClass, superClass);
  }
}
