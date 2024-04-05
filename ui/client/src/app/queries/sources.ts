import { AxiosError } from "axios";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { New, Source } from "@app/api/models";
import {
  createSource,
  deleteSourceById,
  getSBOMSourceById,
  getSources,
  updateSource,
} from "@app/api/rest";

export const SourceQueryKey = "sources";

export const useFetchSources = (refetchDisabled: boolean = false) => {
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: [SourceQueryKey],
    queryFn: () => getSources(),
    refetchInterval: !refetchDisabled ? 5000 : false,
  });
  return {
    sources: data || [],
    isFetching: isLoading,
    fetchError: error,
    refetch,
  };
};

export const useFethSourcesById = (id?: number | string) => {
  const { data, isLoading, error } = useQuery({
    queryKey: [SourceQueryKey, id],
    queryFn: () =>
      id === undefined ? Promise.resolve(undefined) : getSBOMSourceById(id),
    enabled: id !== undefined,
  });

  return {
    credentials: data,
    isFetching: isLoading,
    fetchError: error as AxiosError,
  };
};

export const useCreateSourceMutation = (
  onSuccess: (res: Source) => void,
  onError: (err: AxiosError, payload: New<Source>) => void
) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (obj) => createSource(obj),
    onSuccess: ({ data }, _payload) => {
      onSuccess(data);
      queryClient.invalidateQueries({ queryKey: [SourceQueryKey] });
    },
    onError,
  });
};

export const useUpdateSourceMutation = (
  onSuccess: (payload: Source) => void,
  onError: (err: AxiosError, payload: Source) => void
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (obj) => updateSource(obj),
    onSuccess: (_res, payload) => {
      onSuccess(payload);
      queryClient.invalidateQueries({ queryKey: [SourceQueryKey] });
    },
    onError: onError,
  });
};

export const useDeleteSourceMutation = (
  onSuccess: (id: number | string) => void,
  onError: (err: AxiosError, id: number | string) => void
) => {
  const queryClient = useQueryClient();

  const { isPending, mutate, error } = useMutation({
    mutationFn: (id: string | number) => deleteSourceById(id),
    onSuccess: (_res, id) => {
      onSuccess(id);
      queryClient.invalidateQueries({ queryKey: [SourceQueryKey] });
    },
    onError: (err: AxiosError, id) => {
      onError(err, id);
      queryClient.invalidateQueries({ queryKey: [SourceQueryKey] });
    },
  });

  return {
    mutate,
    isPending,
    error,
  };
};
