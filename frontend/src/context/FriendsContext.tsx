import { createContext, useContext, useEffect, useState } from "react"
import type { ReactNode } from "react"
import { api } from "@/lib/api"
import { useAuth } from "@/context/AuthContext"
import type { Friendship } from "@/lib/types"

type FriendsContextValue = {
  friends: Friendship[]
  requests: Friendship[]
  incomingRequests: Friendship[]
  loading: boolean
  refresh: () => Promise<void>
  sendRequest: (handle: string) => Promise<void>
  acceptRequest: (id: string) => Promise<void>
  removeFriendship: (id: string) => Promise<void>
}

const FriendsContext = createContext<FriendsContextValue | undefined>(undefined)

export function FriendsProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [friends, setFriends] = useState<Friendship[]>([])
  const [requests, setRequests] = useState<Friendship[]>([])
  const [loading, setLoading] = useState(true)

  async function refresh() {
    const [friendsRes, requestsRes] = await Promise.all([
      api.get<Friendship[]>("/friends"),
      api.get<Friendship[]>("/friends/requests"),
    ])
    setFriends(friendsRes.data)
    setRequests(requestsRes.data)
  }

  useEffect(() => {
    if (!user) return
    refresh().finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id])

  // Mutação já devolve o registro atualizado — em vez de reconsultar
  // /friends + /friends/requests, só encaixa o resultado no estado local.
  async function sendRequest(handle: string) {
    const res = await api.post<Friendship>("/friends", { handle })
    setRequests((prev) => [...prev, res.data])
  }

  async function acceptRequest(id: string) {
    const res = await api.post<Friendship>(`/friends/${id}/accept`)
    setRequests((prev) => prev.filter((r) => r.id !== id))
    setFriends((prev) => [...prev, res.data])
  }

  async function removeFriendship(id: string) {
    await api.delete(`/friends/${id}`)
    setRequests((prev) => prev.filter((r) => r.id !== id))
    setFriends((prev) => prev.filter((f) => f.id !== id))
  }

  const incomingRequests = requests.filter((r) => r.incoming)

  return (
    <FriendsContext.Provider
      value={{ friends, requests, incomingRequests, loading, refresh, sendRequest, acceptRequest, removeFriendship }}
    >
      {children}
    </FriendsContext.Provider>
  )
}

export function useFriends() {
  const ctx = useContext(FriendsContext)
  if (!ctx) {
    throw new Error("useFriends precisa estar dentro de um FriendsProvider")
  }
  return ctx
}
