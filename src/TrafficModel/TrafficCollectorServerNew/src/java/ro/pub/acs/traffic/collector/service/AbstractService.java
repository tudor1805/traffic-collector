package ro.pub.acs.traffic.collector.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.pub.acs.traffic.collector.dao.GenericDAO;

@Service("abstractService")
public abstract class AbstractService<T extends Serializable, ID extends Serializable> {

    private GenericDAO<T, ID> dao;

    public T findById(ID id) {
        return dao.findById(id, true);
    }

    public List<T> findAll() {
        return dao.findAll();
    }

    @Transactional
    public T save(T entity) {
        return dao.makePersistent(entity);
    }

    @Transactional
    public void delete(T entity) {
        dao.makeTransient(entity);
    }

    public GenericDAO<T, ID> getDao() {
        return dao;
    }

    public void setDao(GenericDAO<T, ID> dao) {
        this.dao = dao;
    }
}
