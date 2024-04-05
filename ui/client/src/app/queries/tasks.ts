import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AxiosError } from "axios";

import { New, Task } from "@app/api/models";
import { createTask, getTasks } from "@app/api/rest";

export const TasksQueryKey = "tasks";

export const useFetchTasks = (refetchDisabled: boolean = false) => {
  const { isLoading, error, refetch, data } = useQuery({
    queryKey: [TasksQueryKey],
    queryFn: getTasks,
    refetchInterval: !refetchDisabled ? 5000 : false,
  });
  const hasActiveTasks = data && data.length > 0;

  return {
    tasks: data || [],
    isFetching: isLoading,
    fetchError: error,
    refetch,
    hasActiveTasks,
  };
};

export const useCreateTaskMutation = (
  onSuccess: (res: Task) => void,
  onError: (err: AxiosError, payload: Pick<Task, "source">) => void
) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (obj) => createTask(obj),
    onSuccess: ({ data }, _payload) => {
      onSuccess(data);
      queryClient.invalidateQueries({ queryKey: [TasksQueryKey] });
    },
    onError,
  });
};
