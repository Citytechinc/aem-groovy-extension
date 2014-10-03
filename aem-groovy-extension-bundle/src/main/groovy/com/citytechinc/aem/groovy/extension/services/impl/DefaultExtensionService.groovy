package com.citytechinc.aem.groovy.extension.services.impl

import com.citytechinc.aem.groovy.extension.api.MetaClassExtensionProvider
import com.citytechinc.aem.groovy.extension.services.ExtensionService
import groovy.util.logging.Slf4j
import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Reference
import org.apache.felix.scr.annotations.ReferenceCardinality
import org.apache.felix.scr.annotations.ReferencePolicy
import org.apache.felix.scr.annotations.Service
import org.codehaus.groovy.runtime.InvokerHelper

@Service(ExtensionService)
@Component(immediate = true)
@Slf4j("LOG")
class DefaultExtensionService implements ExtensionService {

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        referenceInterface = MetaClassExtensionProvider, policy = ReferencePolicy.DYNAMIC)
    List<MetaClassExtensionProvider> metaClassExtensionProviders = []

    @Override
    Set<Class> getMetaClasses() {
        def metaClasses = [] as LinkedHashSet

        metaClassExtensionProviders.each {
            metaClasses.addAll(it.metaClasses.keySet())
        }

        metaClasses
    }

    void bindMetaClassExtensionProvider(MetaClassExtensionProvider extension) {
        metaClassExtensionProviders.add(extension)

        LOG.info "added metaclass extension provider = {}", extension.class.name

        extension.metaClasses.each { clazz, metaClassClosure ->
            clazz.metaClass(metaClassClosure)

            LOG.info "added metaclass for class = {}", clazz.name
        }
    }

    void unbindMetaClassExtensionProvider(MetaClassExtensionProvider extension) {
        metaClassExtensionProviders.remove(extension)

        LOG.info "removed metaclass extension provider = {}", extension.class.name

        // remove metaclass from registry for each mapped class
        extension.metaClasses.each { clazz, closure ->
            InvokerHelper.metaRegistry.removeMetaClass(clazz)

            LOG.info "removed metaclass for class = {}", clazz.name

            // ensure that valid metaclasses are still registered
            metaClassExtensionProviders.each {
                def metaClassClosure = it.metaClasses[clazz]

                if (metaClassClosure) {
                    LOG.info "retaining metaclass for class = {} from service = {}", clazz.name, it.class.name

                    clazz.metaClass(metaClassClosure)
                }
            }
        }
    }
}
