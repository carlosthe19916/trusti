import { yupResolver } from "@hookform/resolvers/yup";
import { AxiosError } from "axios";
import React, { useContext } from "react";
import { useForm } from "react-hook-form";
import { object, string } from "yup";

import {
  ActionGroup,
  Button,
  ButtonVariant,
  Form,
  FormSelectOption,
} from "@patternfly/react-core";

import { New, Source } from "@app/api/models";
import {
  useCreateSourceMutation,
  useUpdateSourceMutation,
} from "@app/queries/sources";

import {
  HookFormPFSelect,
  HookFormPFTextInput,
} from "@app/components/HookFormPFFields";
import { NotificationsContext } from "@app/components/NotificationsContext";

export interface FormValues {
  type: string;
  url: string;
  git_ref?: string;
  git_workginDirectory?: string;
}

export interface ISourceFormProps {
  source?: Source;
  onClose: () => void;
}

export const SourceForm: React.FC<ISourceFormProps> = ({ source, onClose }) => {
  const { pushNotification } = useContext(NotificationsContext);

  const validationSchema = object().shape({
    type: string().trim().required().min(3).max(120),
    url: string().trim().required().min(3).max(250),
    git_ref: string().trim(),
    git_workginDirectory: string().trim(),
  });

  const {
    handleSubmit,
    formState: { isSubmitting, isValidating, isValid, isDirty },
    getValues,
    control,
  } = useForm<FormValues>({
    defaultValues: {
      type: source?.type || "http",
      url: source?.url || "",
      git_ref: source?.gitDetails?.ref || "",
      git_workginDirectory: source?.gitDetails?.workingDirectory || "",
    },
    resolver: yupResolver(validationSchema),
    mode: "onChange",
  });

  const onCreateSuccess = (_: Source) =>
    pushNotification({
      title: "Source created",
      variant: "success",
    });

  const onCreateError = (error: AxiosError) => {
    pushNotification({
      title: "Error while creating the source",
      variant: "danger",
    });
  };

  const { mutate: createSource } = useCreateSourceMutation(
    onCreateSuccess,
    onCreateError
  );

  const onUpdateSuccess = (_: Source) =>
    pushNotification({
      title: "Source updated",
      variant: "success",
    });

  const onUpdateError = (error: AxiosError) => {
    pushNotification({
      title: "Error while updating the source",
      variant: "danger",
    });
  };
  const { mutate: updateSource } = useUpdateSourceMutation(
    onUpdateSuccess,
    onUpdateError
  );

  const onSubmit = (formValues: FormValues) => {
    const payload: New<Source> = {
      type: formValues.type.trim() as any,
      url: formValues.url?.trim(),
      gitDetails: {
        ref: formValues.git_ref?.trim(),
        workingDirectory: formValues.git_workginDirectory?.trim(),
      },
    };

    if (source) {
      updateSource({ id: source.id, ...payload });
    } else {
      createSource(payload);
    }
    onClose();
  };

  return (
    <Form onSubmit={handleSubmit(onSubmit)}>
      <HookFormPFSelect
        control={control}
        name="type"
        label="Type"
        fieldId="type"
        isRequired
      >
        {["http", "git"].map((option, index) => (
          <FormSelectOption key={index} value={option} label={option} />
        ))}
      </HookFormPFSelect>
      <HookFormPFTextInput
        control={control}
        name="url"
        label="URL"
        fieldId="url"
        isRequired
      />

      <ActionGroup>
        <Button
          type="submit"
          aria-label="submit"
          id="source-form-submit"
          variant={ButtonVariant.primary}
          isDisabled={!isValid || isSubmitting || isValidating || !isDirty}
        >
          {!source ? "Create" : "Save"}
        </Button>
        <Button
          type="button"
          id="cancel"
          aria-label="cancel"
          variant={ButtonVariant.link}
          isDisabled={isSubmitting || isValidating}
          onClick={onClose}
        >
          Cancel
        </Button>
      </ActionGroup>
    </Form>
  );
};
