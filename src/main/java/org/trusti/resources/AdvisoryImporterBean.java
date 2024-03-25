package org.trusti.resources;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.trusti.models.AdvisoryMetadata;
import org.trusti.models.jpa.AdvisoryRepository;
import org.trusti.models.jpa.entity.AdvisoryEntity;
import schemas.csaf.AggregateSeverity;
import schemas.csaf.Csaf;
import schemas.csaf.Document;
import schemas.csaf.Tracking;
import schemas.cve.v5.CveV5;
import schemas.osv.Osv;
import schemas.osv.Severity;

import java.util.Date;
import java.util.Optional;

@ApplicationScoped
@Named("AdvisoryImporterBean")
@RegisterForReflection
public class AdvisoryImporterBean {

    @Inject
    AdvisoryRepository advisoryRepository;

    @Transactional
    public void csaf(@Body Csaf csaf, @Header("taskId") Long taskId, Exchange exchange) {
        Optional<String> identifier = Optional.ofNullable(csaf.getDocument()).map(Document::getTracking).map(Tracking::getId);
        Optional<String> title = Optional.ofNullable(csaf.getDocument()).map(Document::getTitle);
        Optional<String> aggregateSeverity = Optional.ofNullable(csaf.getDocument()).map(Document::getAggregateSeverity).map(AggregateSeverity::getText);
        Optional<Date> currentReleaseDate = Optional.ofNullable(csaf.getDocument()).map(Document::getTracking).map(Tracking::getCurrentReleaseDate);

        AdvisoryMetadata metadata = new AdvisoryMetadata(
                identifier.orElse(null),
                title.orElse(null),
                aggregateSeverity.orElse(null),
                currentReleaseDate.orElse(null)
        );

        AdvisoryEntity advisoryEntity = advisoryRepository.create(metadata, taskId);

        exchange.getIn().setBody(advisoryEntity);
    }

    public void osv(@Body Osv osv, @Header("taskId") Long taskId, Exchange exchange) {
        Optional<String> identifier = Optional.ofNullable(osv.getId());
        Optional<String> title = Optional.ofNullable(osv.getSummary());
        Optional<String> aggregateSeverity = osv.getSeverity().stream().map(Severity::getScore).findFirst();
        Optional<Date> currentReleaseDate = Optional.ofNullable(osv.getPublished());

        AdvisoryMetadata metadata = new AdvisoryMetadata(
                identifier.orElse(null),
                title.orElse(null),
                aggregateSeverity.orElse(null),
                currentReleaseDate.orElse(null)
        );

        AdvisoryEntity advisoryEntity = advisoryRepository.create(metadata, taskId);
        exchange.getIn().setBody(advisoryEntity);
    }

    public void cve_v5(@Body CveV5 cveV5, @Header("taskId") Long taskId, Exchange exchange) {
        AdvisoryMetadata metadata = new AdvisoryMetadata(
                null,
                null,
                null,
                null
        );

        AdvisoryEntity advisoryEntity = advisoryRepository.create(metadata, taskId);
        exchange.getIn().setBody(advisoryEntity);
    }
}
