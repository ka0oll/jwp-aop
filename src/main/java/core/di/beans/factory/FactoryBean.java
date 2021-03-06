package core.di.beans.factory;

public interface FactoryBean<T> {
    T getObject() throws Exception;
}
