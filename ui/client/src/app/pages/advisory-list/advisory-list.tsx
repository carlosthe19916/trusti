import React from "react";

import {
  Button,
  ButtonVariant,
  PageSection,
  PageSectionVariants,
  Text,
  TextContent,
  ToolbarContent,
  ToolbarItem,
} from "@patternfly/react-core";

import { useAdvisoryList } from "./useAdvisoryList";
import { UploadFilesDrawer } from "./components/upload-files-drawer";

export const AdvisoryList: React.FC = () => {
  const { tableProps, table } = useAdvisoryList();

  const {
    components: { Toolbar, FilterToolbar, PaginationToolbarItem, Pagination },
  } = tableProps;

  const [showUploadComponent, setShowUploadComponent] = React.useState(false);

  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">Advisories</Text>
        </TextContent>
      </PageSection>
      <PageSection>
        <div
          style={{
            backgroundColor: "var(--pf-v5-global--BackgroundColor--100)",
          }}
        >
          <Toolbar>
            <ToolbarContent>
              <FilterToolbar
                id="advisory-toolbar"
                {...{ showFiltersSideBySide: true }}
              />
              <ToolbarItem>
                <Button
                  type="button"
                  id="upload-files"
                  aria-label="Upload files"
                  variant={ButtonVariant.secondary}
                  onClick={() => setShowUploadComponent(true)}
                >
                  Upload files
                </Button>
              </ToolbarItem>
              <PaginationToolbarItem>
                <Pagination
                  variant="top"
                  isCompact
                  widgetId="advisories-pagination-top"
                />
              </PaginationToolbarItem>
            </ToolbarContent>
          </Toolbar>
          {table}
        </div>

        <UploadFilesDrawer
          isExpanded={showUploadComponent}
          onCloseClick={() => setShowUploadComponent(false)}
        />
      </PageSection>
    </>
  );
};
