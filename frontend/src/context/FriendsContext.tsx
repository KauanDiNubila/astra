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

  const incomingRequests = requests.filter((r) => r.incoming)

  return (
    <FriendsContext.Provider value={{ friends, requests, incomingRequests, loading, refresh }}>
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
