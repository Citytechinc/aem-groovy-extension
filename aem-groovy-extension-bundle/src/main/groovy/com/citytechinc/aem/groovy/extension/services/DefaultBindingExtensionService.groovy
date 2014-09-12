package com.citytechinc.aem.groovy.extension.services

import groovy.util.logging.Slf4j

import javax.jcr.Session

import org.apache.felix.scr.annotations.Activate
import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Reference
import org.apache.felix.scr.annotations.Service
import org.apache.sling.api.SlingHttpServletRequest
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory

import com.citytechinc.aem.groovy.extension.api.BindingExtensionService
import com.citytechinc.aem.groovy.extension.builders.NodeBuilder
import com.citytechinc.aem.groovy.extension.builders.PageBuilder
import com.day.cq.search.QueryBuilder
import com.day.cq.wcm.api.PageManager

@Service
@Component(immediate = true)
@Slf4j("LOG")
class DefaultBindingExtensionService implements BindingExtensionService {

	@Reference
	QueryBuilder queryBuilder

	BundleContext bundleContext

	@Override
	Map<String, ?> getBindings(SlingHttpServletRequest request) {

		def resourceResolver = request.resourceResolver
		def session = resourceResolver.adaptTo(Session)

		[
			log: LoggerFactory.getLogger("groovyconsole"),
			session: session,
			slingRequest: request,
			pageManager: resourceResolver.adaptTo(PageManager),
			resourceResolver: resourceResolver,
			queryBuilder: queryBuilder,
			nodeBuilder: new NodeBuilder(session),
			pageBuilder: new PageBuilder(session),
			bundleContext: bundleContext
		]
	}

	@Activate
	void activate(BundleContext bundleContext) {
		this.bundleContext = bundleContext
	}
}