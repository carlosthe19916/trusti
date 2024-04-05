import { AxiosError } from "axios";
import React from "react";
import { NavLink } from "react-router-dom";

import {
  Button,
  ButtonVariant,
  Modal,
  ModalVariant,
  PageSection,
  PageSectionVariants,
  Text,
  TextContent,
  ToolbarContent,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import {
  ActionsColumn,
  ExpandableRowContent,
  Td as PFTd,
  Th as PFTh,
  Tr as PFTr,
} from "@patternfly/react-table";
import PlayIcon from "@patternfly/react-icons/dist/js/icons/play-icon";

import { Source, Task } from "@app/api/models";
import { getAxiosErrorMessage } from "@app/utils/utils";

import { ConfirmDialog } from "@app/components/ConfirmDialog";
import { NotificationsContext } from "@app/components/NotificationsContext";
import { useDeleteSourceMutation, useFetchSources } from "@app/queries/sources";
import {
  ConditionalTableBody,
  FilterType,
  useClientTableBatteries,
} from "@carlosthe19916-latest/react-table-batteries";

import { SourceForm } from "./components/source-form";
import { useCreateTaskMutation, useFetchTasks } from "@app/queries/tasks";
import { SourceImportStatus } from "./components/source-analysis-status";

export const SourceList: React.FC = () => {
  const { pushNotification } = React.useContext(NotificationsContext);

  const [isRunTaskConfirmDialogOpen, setIsRunTaskConfirmDialogOpen] =
    React.useState<boolean>(false);
  const [sourceToRun, setSourceToRun] = React.useState<Source>();

  const [isDeleteConfirmDialogOpen, setIsDeleteConfirmDialogOpen] =
    React.useState<boolean>(false);
  const [sourceToDelete, setSourceToDelete] = React.useState<Source>();

  const [createUpdateModalState, setCreateUpdateModalState] = React.useState<
    "create" | Source | null
  >(null);
  const isCreateUpdateModalOpen = createUpdateModalState !== null;
  const entityToUpdate =
    createUpdateModalState !== "create" ? createUpdateModalState : null;

  const onDeleteOrgSuccess = () => {
    pushNotification({
      title: "Source created",
      variant: "success",
    });
  };

  const onDeleteOrgError = (error: AxiosError) => {
    pushNotification({
      title: getAxiosErrorMessage(error),
      variant: "danger",
    });
  };

  const getTask = (source: Source) =>
    tasks.find((task: Task) => task.source?.id === source.id);

  const { tasks, hasActiveTasks } = useFetchTasks(
    isRunTaskConfirmDialogOpen ||
      isDeleteConfirmDialogOpen ||
      createUpdateModalState !== null
  );

  const { sources, isFetching, fetchError, refetch } =
    useFetchSources(hasActiveTasks);

  const { mutate: deleteSource } = useDeleteSourceMutation(
    onDeleteOrgSuccess,
    onDeleteOrgError
  );

  const { mutate: createTask } = useCreateTaskMutation(
    onDeleteOrgSuccess,
    onDeleteOrgError
  );

  const tableControls = useClientTableBatteries({
    persistTo: "state",
    idProperty: "id",
    items: sources,
    isLoading: isFetching,
    columnNames: {
      id: "Id",
      type: "Type",
      url: "url",
    },
    hasActionsColumn: true,
    filter: {
      isEnabled: true,
      filterCategories: [
        {
          key: "q",
          title: "URL",
          type: FilterType.search,
          placeholderText: "Filter by url...",
          getItemValue: (item) => item.url || "",
        },
      ],
    },
    sort: {
      isEnabled: true,
      sortableColumns: ["id", "type", "url"],
      getSortValues: (project) => ({
        id: project?.id || "",
        type: project?.type || "",
        url: project?.url || "",
      }),
    },
    pagination: { isEnabled: true },
    expansion: {
      isEnabled: true,
      variant: "single",
    },
  });

  const {
    currentPageItems,
    numRenderedColumns,
    components: {
      Table,
      Thead,
      Tr,
      Th,
      Tbody,
      Td,
      Toolbar,
      FilterToolbar,
      PaginationToolbarItem,
      Pagination,
    },
    expansion: { isCellExpanded },
  } = tableControls;

  const closeCreateUpdateModal = () => {
    setCreateUpdateModalState(null);
    refetch;
  };

  const deleteRow = (row: Source) => {
    setSourceToDelete(row);
    setIsDeleteConfirmDialogOpen(true);
  };

  const runTask = (row: Source) => {
    setSourceToRun(row);
    setIsRunTaskConfirmDialogOpen(true);
  };

  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">Sources</Text>
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
              <FilterToolbar id="source-toolbar" />
              <ToolbarGroup variant="button-group">
                <ToolbarItem>
                  <Button
                    type="button"
                    id="create-source"
                    aria-label="Create new source"
                    variant={ButtonVariant.primary}
                    onClick={() => setCreateUpdateModalState("create")}
                  >
                    Create Source
                  </Button>
                </ToolbarItem>
              </ToolbarGroup>
              <PaginationToolbarItem>
                <Pagination
                  variant="top"
                  isCompact
                  widgetId="sources-pagination-top"
                />
              </PaginationToolbarItem>
            </ToolbarContent>
          </Toolbar>

          <Table aria-label="Sources table">
            <Thead>
              <Tr isHeaderRow>
                <Th columnKey="id" />
                <Th columnKey="type" />
                <Th columnKey="url" />
                <PFTh>Actions</PFTh>
              </Tr>
            </Thead>
            <ConditionalTableBody
              isLoading={isFetching}
              isError={!!fetchError}
              isNoData={sources.length === 0}
              numRenderedColumns={numRenderedColumns}
            >
              {currentPageItems?.map((item, rowIndex) => {
                return (
                  <Tbody key={item.id}>
                    <Tr item={item} rowIndex={rowIndex}>
                      <Td width={15} columnKey="id">
                        <NavLink to={`/sources/${item.id}`}>{item.id}</NavLink>
                      </Td>
                      <Td width={20} modifier="truncate" columnKey="type">
                        {item.type}
                      </Td>
                      <Td width={50} modifier="truncate" columnKey="url">
                        {item.url}
                      </Td>
                      <PFTd width={10} modifier="truncate">
                        <SourceImportStatus
                          state={getTask(item)?.state || "No task"}
                        />
                      </PFTd>
                      <PFTd isActionCell>
                        <ActionsColumn
                          items={[
                            {
                              title: "Run",
                              onClick: () => runTask(item),
                            },
                            {
                              isSeparator: true
                            },
                            {
                              title: "Edit",
                              onClick: () => setCreateUpdateModalState(item),
                            },
                            {
                              title: "Delete",
                              onClick: () => deleteRow(item),
                            },
                          ]}
                        />
                      </PFTd>
                    </Tr>
                    {isCellExpanded(item) ? (
                      <PFTr isExpanded>
                        <PFTd colSpan={7}>
                          <ExpandableRowContent>
                            Some content here
                          </ExpandableRowContent>
                        </PFTd>
                      </PFTr>
                    ) : null}
                  </Tbody>
                );
              })}
            </ConditionalTableBody>
          </Table>
          <Pagination
            variant="bottom"
            isCompact
            widgetId="sources-pagination-bottom"
          />
        </div>
      </PageSection>

      <Modal
        id="create-edit-source-modal"
        title={entityToUpdate ? "Update Source" : "New Source"}
        variant={ModalVariant.medium}
        isOpen={isCreateUpdateModalOpen}
        onClose={closeCreateUpdateModal}
      >
        <SourceForm
          source={entityToUpdate ? entityToUpdate : undefined}
          onClose={closeCreateUpdateModal}
        />
      </Modal>

      {isDeleteConfirmDialogOpen && (
        <ConfirmDialog
          title="Delete source"
          isOpen={true}
          titleIconVariant={"warning"}
          message="Are you sure you want to delete the Source?"
          confirmBtnVariant={ButtonVariant.danger}
          confirmBtnLabel="Delete"
          cancelBtnLabel="Cancel"
          onCancel={() => setIsDeleteConfirmDialogOpen(false)}
          onClose={() => setIsDeleteConfirmDialogOpen(false)}
          onConfirm={() => {
            if (sourceToDelete) {
              deleteSource(sourceToDelete.id);
              setSourceToDelete(undefined);
            }
            setIsDeleteConfirmDialogOpen(false);
          }}
        />
      )}

      {isRunTaskConfirmDialogOpen && (
        <ConfirmDialog
          title="Run task"
          isOpen={true}
          titleIconVariant={"info"}
          message="Are you sure you want to run this task?"
          confirmBtnVariant={ButtonVariant.primary}
          confirmBtnLabel="Run"
          cancelBtnLabel="Cancel"
          onCancel={() => setIsRunTaskConfirmDialogOpen(false)}
          onClose={() => setIsRunTaskConfirmDialogOpen(false)}
          onConfirm={() => {
            if (sourceToRun) {
              const task: Pick<Task, "source"> = {
                source: sourceToRun,
              };
              createTask(task);
              setSourceToRun(undefined);
            }
            setIsRunTaskConfirmDialogOpen(false);
          }}
        />
      )}
    </>
  );
};
